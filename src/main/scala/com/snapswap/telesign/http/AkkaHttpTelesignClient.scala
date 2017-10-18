package com.snapswap.telesign.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.Materializer
import com.snapswap.telesign._
import com.snapswap.telesign.model.external._
import com.snapswap.telesign.model.internal.{SmsVerifyResponse, VerifyResponse}
import com.snapswap.telesign.unmarshaller.UnmarshallerVerify
import spray.json._

import scala.concurrent.Future

class AkkaHttpTelesignClient(override val customerId: String,
                             override val apiKey: String,
                             useCaseCode: EnumUseCaseCodes.UseCaseCode)
                            (implicit override val system: ActorSystem,
                             override val materializer: Materializer)
  extends TelesignClient
    with HttpMethods
    with UnmarshallerVerify {

  override val domain: String = "rest-ww.telesign.com"
  override val baseURL: String = "/v1"

  private val log = Logging(system, this.getClass)

  override def getScore(number: String): Future[PhoneScore] =
    send(get(s"/phoneid/score/$number?ucid=$useCaseCode")) { responseStr =>
      responseStr.parseJson.convertTo[PhoneScore]
    }

  override def initiateVerification(number: String, code: String): Future[PhoneVerificationId] =
    send(
      post(s"/verify/sms",
        Map(
          "phone_number" -> number,
          "language" -> "en-US",
          "template" -> code
        )
      )
    ) { responseStr =>
      val response = responseStr.parseJson.convertTo[SmsVerifyResponse]

      PhoneVerificationId(response.referenceId)
    }

  override def getVerification(id: PhoneVerificationId): Future[PhoneVerification] =
    send(get(s"/verify/${id.value}")) { responseStr =>
      val response = responseStr.parseJson.convertTo[VerifyResponse]


      PhoneVerification(id,
        response.errors,
        EnumSmsStatusCodes.withCode(response.status.code)
      )
    }

}
