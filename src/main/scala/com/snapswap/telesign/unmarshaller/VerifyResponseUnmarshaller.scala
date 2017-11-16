package com.snapswap.telesign.unmarshaller

import com.snapswap.telesign.model.external.PhoneVerificationId
import com.snapswap.telesign.model.external.exceptions.{TelesignError, TelesignResponseFailure}
import com.snapswap.telesign.model.internal._
import spray.json._

import scala.util.{Failure, Success, Try}

trait VerifyResponseUnmarshaller {
  this: CommonJsonSupport with DefaultJsonProtocol =>

  private implicit val verifyCodeStateEnumFormat = enumNameFormat(EnumVerifyCodeState)
  private implicit val verifyFormat = jsonFormat(Verify, "code_state", "code_entered")
  private implicit val verifyResponseFormat = jsonFormat(VerifyResponse,
    "reference_id", "resource_uri", "sub_resource", "errors", "status", "verify"
  )

  implicit object PhoneVerificationIdFormat extends RootJsonReader[PhoneVerificationId] {
    override def read(json: JsValue): PhoneVerificationId = Try {
      val response = json.convertTo[VerifyResponse]

      if (Status.isFailed(response.status)) {
        throw TelesignResponseFailure(response.status, json)
      } else if (response.errors.nonEmpty) {
        throw TelesignResponseFailure(response.errors, json)
      } else {
        val referenceId = response.referenceId
          .getOrElse(throw TelesignResponseFailure("can't parse reference_id", json))
        PhoneVerificationId(referenceId)
      }
    } match {
      case Failure(ex: TelesignError) =>
        throw ex
      case Success(referenceId) =>
        referenceId
      case Failure(ex) =>
        deserializationError(s"can't parse response into PhoneVerificationId, raw response: ${json.compactPrint}", ex)
    }
  }

}