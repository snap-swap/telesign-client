package com.snapswap.telesign.http

import java.net.URLEncoder
import java.util.Base64

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.client.RequestBuilding.{Get, Post, Put}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.util.Timeout
import com.snapswap.http.client.model.{EnrichedRequest, RequestId}
import com.snapswap.http.client.{ConnectionParams, HttpClient}
import com.snapswap.telesign.model.external.exceptions.TelesignRequestFailure
import com.snapswap.telesign.unmarshaller.UnmarshallerVerify
import com.snapswap.telesign.utils.DateTimeHelper._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait HttpMethods {
  unmarshaller: UnmarshallerVerify =>

  implicit def system: ActorSystem

  implicit def materializer: Materializer

  protected def customerId: String

  protected def apiKey: String

  protected def domain: String

  protected def baseURL: String

  protected def log: LoggingAdapter

  private implicit def ec: ExecutionContext = system.dispatcher

  private lazy val decodedKey: Array[Byte] = Base64.getDecoder.decode(apiKey)
  private lazy val signingKey = new SecretKeySpec(decodedKey, "HmacSHA256")
  private lazy val mac: Mac = {
    val _mac = Mac.getInstance("HmacSHA256")
    _mac.init(signingKey)
    _mac
  }
  private lazy val client = HttpClient(
    connectionParams = ConnectionParams.HttpsConnectionParams(domain, 443),
    logger = Some(log)
  )
  private implicit val requestTimeout: Timeout = 20.seconds


  def get(path: String): HttpRequest =
    Get(baseURL + path)

  def put(path: String): HttpRequest =
    Put(baseURL + path)

  def post(path: String, fields: Map[String, String]): HttpRequest = {
    val endpoint = baseURL + path
    log.info(s"Preparing request: POST [$endpoint] with [$fields]")
    Post(endpoint, FormData(fields))
  }

  def send[T](request: HttpRequest)(handler: String => T): Future[T] = {
    http(request)
      .map {
        case Success(response) =>
          response
        case Failure(ex) =>
          log.error(ex, s"error occurred during requesting telesign with $request")
          throw ex
      }
      .flatMap { response =>
        Unmarshal(response.entity)
          .to[String]
          .map { asString =>
            if (response.status.isSuccess) {
              log.debug(s"SUCCESS ${request.method} ${request.uri} -> ${response.status} '$asString'")
              asString
            } else {
              log.error(s"FAILURE ${request.method} ${request.uri} -> ${response.status} '$asString'")
              throw TelesignRequestFailure(response.status, asString)
            }
          }.map(handler)
      }
  }


  private def http(request: HttpRequest): Future[Try[HttpResponse]] = {
    val authMethodHeader = RawHeader("x-ts-auth-method", "HMAC-SHA256")
    val dateHeader = RawHeader("x-ts-date", nowUTC().format(RFC2616))
    val signatureHeader: String => HttpHeader = signature =>
      RawHeader("Authorization", s"TSA $customerId:$signature")

    val (contentType, requestBody) =
      if (request.method == HttpMethods.POST) {
        request.entity.contentType.toString() -> Unmarshal(request.entity).to[FormData].map { form =>
          form.fields.map {
            case (key, value) =>
              s"$key=${URLEncoder.encode(value, "UTF-8")}"
          }.mkString("&")
        }.map(d => s"\n$d")
      } else {
        "" -> Future.successful("")
      }

    requestBody.map { body =>
      s"""${request.method.value}
         |$contentType
         |
         |${authMethodHeader.name}:${authMethodHeader.value}
         |${dateHeader.name}:${dateHeader.value}$body
         |${request.uri.path}""".stripMargin
    }.flatMap { data =>
      val rawHmac = mac.doFinal(data.getBytes())
      val signature = new String(Base64.getEncoder.encode(rawHmac))
      val signedRequest = request.withHeaders(signatureHeader(signature), dateHeader, authMethodHeader)

      val requestId = RequestId.random()
      log.debug(s"sending request $requestId: $signedRequest")

      client.send(EnrichedRequest(signedRequest, requestId)).map(_.response)
    }
  }

}
