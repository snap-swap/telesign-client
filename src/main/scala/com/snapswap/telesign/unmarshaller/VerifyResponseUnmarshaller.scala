package com.snapswap.telesign.unmarshaller

import com.snapswap.telesign.model.external.PhoneVerificationId
import com.snapswap.telesign.model.external.exceptions.{TelesignException, TelesignResponseFailure}
import com.snapswap.telesign.model.internal._
import spray.json._

import scala.util.{Failure, Success, Try}

trait VerifyResponseUnmarshaller {
  this: CommonJsonSupport with DefaultJsonProtocol =>

  implicit val deviceFormat = jsonFormat(Device, "phone_number", "operating_system", "language")
  implicit val appFormat = jsonFormat(App, "signature", "created_on_utc")

  implicit val callForwardActionEnumFormat = enumNameFormat(EnumCallForwardAction)
  implicit val callForwardEnumFormat = enumNameFormat(EnumCallForward)
  implicit val enumUserResponseSelectionFormat = enumNameFormat(EnumUserResponseSelection)
  implicit val verifyCodeStateEnumFormat = enumNameFormat(EnumVerifyCodeState)

  implicit val callForwardingFormat = jsonFormat(CallForwarding, "action", "call_forward")
  implicit val userResponseFormat = jsonFormat(UserResponse, "received", "verification_code", "selection")
  implicit val verifyFormat = jsonFormat(Verify, "code_state", "code_entered")

  implicit val verifyResponseFormat = jsonFormat(VerifyResponse, "reference_id", "resource_uri", "sub_resource", "errors", "status", "verify")
  implicit val smsVerifyResponseFormat = jsonFormat(SmsVerifyResponse, "reference_id", "resource_uri", "sub_resource", "errors", "status", "user_response")

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
      case Failure(ex: TelesignException) =>
        throw ex
      case Success(referenceId) =>
        referenceId
      case Failure(ex) =>
        deserializationError(s"can't parse response into PhoneVerificationId, raw response: ${json.compactPrint}", ex)
    }
  }

}