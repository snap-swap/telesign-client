package com.snapswap.telesign.model.external

import akka.http.scaladsl.model.StatusCode
import com.snapswap.telesign.model.internal.Status

import scala.util.control.NoStackTrace

trait TelesignException extends NoStackTrace {
  def details: String

  override def getMessage: String = details
}

case class TelesignInvalidPhoneNumber(details: String) extends TelesignException

object TelesignInvalidPhoneNumber {
  def apply(cleansingCode: Int): Option[TelesignInvalidPhoneNumber] = {
    val details: Option[String] = cleansingCode match {
      case 105 => Some("The phone number length is too long or too short")
      case 104 => Some("The phone number is not correctly formatted")
      case 103 => Some("The phone number appears to be formatted correctly, but cannot be matched to any specific area")
      case 102 => Some("The phone number entered is a restricted phone number")
      case other => None
    }
    details.map(s => TelesignInvalidPhoneNumber(s))
  }
}

case class TelesignRequestFailure(details: String) extends TelesignException

object TelesignRequestFailure {
  def apply(failedStatus: Status): TelesignRequestFailure = {
    require(Status.isFailed(failedStatus))
    val details = s"Internal Telesign code wasn't successful ${failedStatus.code}: ${failedStatus.description}"
    new TelesignRequestFailure(details)
  }

  def apply(httpStatus: StatusCode, responseBody: Option[String] = None): TelesignRequestFailure = {
    require(!httpStatus.isSuccess())
    val details = s"Http response wasn't successful ${httpStatus.intValue()}: ${httpStatus.defaultMessage()}"
    new TelesignRequestFailure(s"$details${responseBody.map { b => s" response body: $b" }.getOrElse("")}")
  }

  def apply(httpStatus: StatusCode, responseBody: String): TelesignRequestFailure =
    apply(httpStatus, Some(responseBody))
}