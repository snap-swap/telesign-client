package com.snapswap.telesign.unmarshaller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.snapswap.telesign.model.external._
import com.snapswap.telesign.model.external.exceptions.{TelesignException, TelesignInvalidPhoneNumber, TelesignResponseFailure}
import com.snapswap.telesign.model.internal.{Carrier, CleansingNumber, Coordinates, Country, Location, Number, Numbering, OriginalNumber, PhoneIdResponse, PhoneType, Status, TimeZone}
import spray.json._

import scala.util.{Failure, Success, Try}

trait PhoneScoringResponseUnmarshaller {
  this: CommonJsonSupport with DefaultJsonProtocol =>

  implicit val phoneTypeFormat = jsonFormat2(PhoneType)
  implicit val originalNumberFormat = jsonFormat(OriginalNumber, "phone_number", "complete_phone_number", "country_code")
  implicit val numberFormat = jsonFormat(Number, "phone_number", "country_code", "min_length", "max_length", "cleansed_code")
  implicit val cleansingNumberFormat = jsonFormat2(CleansingNumber)
  implicit val numberingFormat = jsonFormat2(Numbering)
  implicit val countryFormat = jsonFormat3(Country)
  implicit val timeZoneFormat = jsonFormat(TimeZone, "utc_offset_min", "name", "utc_offset_max")
  implicit val coordinatesFormat = jsonFormat2(Coordinates)
  implicit val carrierFormat = jsonFormat1(Carrier)
  implicit val locationFormat = jsonFormat(Location, "county", "city", "state", "zip", "country", "time_zone", "coordinates", "metro_code")
  implicit val phoneIdResponseFormat = jsonFormat(PhoneIdResponse, "reference_id", "resource_uri", "sub_resource", "phone_type", "signature_string", "status", "numbering", "location", "carrier", "risk")


  implicit object TelesignPhoneScoreFormat extends RootJsonReader[TelesignPhoneScore] {

    override def read(json: JsValue): TelesignPhoneScore = Try {
      val response: PhoneIdResponse = json.convertTo[PhoneIdResponse]

      if (Status.isFailed(response.status)) {
        throw TelesignResponseFailure(response.status, json)
      }

      val number = response.numbering.flatMap(_.cleansing.map(_.call).flatMap { case Number(phone, code, _, _, cleansedCode) =>
        TelesignInvalidPhoneNumber(cleansedCode).map(throw _)
          .orElse(Option(s"$code$phone"))
      }).getOrElse(throw TelesignInvalidPhoneNumber(s"E.164 phone number is not detected, response body: ${json.compactPrint}"))

      val (riskLevel, riskScore) = response.risk.map { r =>
        RiskLevelEnum.withName(r.level) -> r.score
      }.getOrElse(RiskLevelEnum.high -> 1000)

      val phoneType = response.phoneType.flatMap { t =>
        Try(EnumPhoneTypes.withId(t.code.toInt)).toOption
      }.getOrElse(EnumPhoneTypes.Other)

      val carrier = response.carrier.map(_.name).getOrElse("Unknown")

      val timestamp = ZonedDateTime.parse(response.status.updatedOn, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      TelesignPhoneScore(
        phone = number,
        riskLevel = riskLevel,
        riskScore = riskScore,
        phoneType = phoneType,
        carrier = carrier,
        updatedOn = timestamp
      )

    } match {
      case Success(score) =>
        score
      case Failure(ex: TelesignException) =>
        throw ex
      case Failure(ex) =>
        deserializationError(s"can't parse response into TelesignPhoneScore, raw response: ${json.compactPrint}", ex)
    }
  }

}
