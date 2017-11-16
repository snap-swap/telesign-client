package com.snapswap.telesign.unmarshaller

import com.snapswap.telesign.model.external.exceptions.{TelesignError, TelesignResponseFailure}
import com.snapswap.telesign.model.internal.{CompletionDone, CompletionResponse, Status}
import spray.json._

import scala.util.{Failure, Success, Try}

trait CompletionResponseUnmarshaller {
  this: CommonJsonSupport with DefaultJsonProtocol =>

  private implicit val completionResponseFormat = jsonFormat(CompletionResponse,
    "reference_id", "resource_uri", "sub_resource", "errors", "status"
  )

  implicit object CompletionDoneFormat extends RootJsonReader[CompletionDone] {
    override def read(json: JsValue): CompletionDone = Try {
      val response = json.convertTo[CompletionResponse]

      if (Status.isFailed(response.status)) {
        throw TelesignResponseFailure(response.status, json)
      } else if (response.errors.nonEmpty) {
        throw TelesignResponseFailure(response.errors, json)
      } else {
        CompletionDone()
      }

    } match {
      case Failure(ex: TelesignError) =>
        throw ex
      case Success(done) =>
        done
      case Failure(ex) =>
        deserializationError(s"error during parsing completion response, raw response: ${json.compactPrint}", ex)
    }
  }

}
