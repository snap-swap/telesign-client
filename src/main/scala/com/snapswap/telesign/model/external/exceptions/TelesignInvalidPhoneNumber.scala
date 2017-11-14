package com.snapswap.telesign.model.external.exceptions


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