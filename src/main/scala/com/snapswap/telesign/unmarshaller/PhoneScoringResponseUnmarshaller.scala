package com.snapswap.telesign.unmarshaller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.snapswap.telesign.model.external._
import com.snapswap.telesign.model.external.exceptions.{TelesignError, TelesignInvalidPhoneNumber, TelesignResponseFailure}
import com.snapswap.telesign.model.internal.{Carrier, CleansingNumber, Coordinates, Country, Location, Number, Numbering, OriginalNumber, PhoneType, ScoreResponse, Status, TimeZone}
import spray.json._

import scala.util.{Failure, Success, Try}

trait PhoneScoringResponseUnmarshaller {
  this: CommonJsonSupport with DefaultJsonProtocol =>

  private implicit val phoneTypeFormat = jsonFormat2(PhoneType)
  private implicit val originalNumberFormat = jsonFormat(OriginalNumber,
    "phone_number", "complete_phone_number", "country_code"
  )
  private implicit val numberFormat = jsonFormat(Number,
    "phone_number", "country_code", "min_length", "max_length", "cleansed_code"
  )
  private implicit val cleansingNumberFormat = jsonFormat2(CleansingNumber)
  private implicit val numberingFormat = jsonFormat2(Numbering)
  private implicit val countryFormat = jsonFormat3(Country)
  private implicit val timeZoneFormat = jsonFormat(TimeZone,
    "utc_offset_min", "name", "utc_offset_max"
  )
  private implicit val coordinatesFormat = jsonFormat2(Coordinates)
  private implicit val carrierFormat = jsonFormat1(Carrier)
  private implicit val locationFormat = jsonFormat(Location,
    "county", "city", "state", "zip", "country", "time_zone", "coordinates", "metro_code"
  )
  private implicit val phoneIdResponseFormat = jsonFormat(ScoreResponse,
    "reference_id", "resource_uri", "sub_resource", "phone_type", "signature_string",
    "status", "numbering", "location", "carrier", "risk"
  )


  implicit object TelesignPhoneScoreFormat extends RootJsonReader[TelesignPhoneScore] {

    override def read(json: JsValue): TelesignPhoneScore = Try {
      val response: ScoreResponse = json.convertTo[ScoreResponse]

      if (Status.isFailed(response.status)) {
        throw TelesignResponseFailure(response.status, json)
      }

      val (number: String, countryCode: String) = response
        .numbering
        .flatMap(
          _.cleansing.map(_.call).flatMap {
            case Number(phone, code, _, _, cleansedCode) =>
              TelesignInvalidPhoneNumber(cleansedCode)
                .map(throw _)
                .orElse(Option(Tuple2(s"$code$phone", code)))
          })
        .getOrElse(throw TelesignInvalidPhoneNumber(s"E.164 phone number is not detected, response body: ${json.compactPrint}"))

      val (riskLevel, riskScore) = response.risk.map { r =>
        RiskLevelEnum.withName(r.level) -> r.score
      }.getOrElse(RiskLevelEnum.high -> 1000)

      val phoneType = response.phoneType.flatMap { t =>
        Try(EnumPhoneTypes.withId(t.code.toInt)).toOption
      }.getOrElse(EnumPhoneTypes.Other)

      val carrier = response.carrier.map(_.name).getOrElse("Unknown")

      val timestamp = ZonedDateTime.parse(response.status.updatedOn, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      val iso3CountryCode =
        response.location.flatMap(_.country).map(_.iso3)
          .getOrElse(throw TelesignResponseFailure("can't parse iso3 country code", json))

      TelesignPhoneScore(
        phone = number,
        riskLevel = riskLevel,
        riskScore = riskScore,
        phoneType = phoneType,
        carrier = carrier,
        updatedOn = timestamp,
        iso3CountryCode = iso3CountryCode,
        countryCode = countryCode
      )

    } match {
      case Success(score) =>
        score
      case Failure(ex: TelesignError) =>
        throw ex
      case Failure(ex) =>
        deserializationError(s"can't parse response into TelesignPhoneScore, raw response: ${json.compactPrint}", ex)
    }
  }

}
