package com.snapswap.telesign.unmarshaller

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.snapswap.telesign.model.external._
import com.snapswap.telesign.model.internal.{Carrier, CleansingNumber, Coordinates, Country, Location, Number, Numbering, OriginalNumber, PhoneIdResponse, PhoneType, Status, TimeZone}
import spray.json._

import scala.util.{Failure, Success, Try}

trait PhoneIdResponseUnmarshaller
  extends DefaultJsonProtocol {
  this: CommonJsonSupport =>

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


  implicit object TelesignPhoneScoreFormat extends RootJsonReader[TelesignPhoneScore]{

    override def read(json: JsValue): TelesignPhoneScore = Try{
      val response: PhoneIdResponse = phoneIdResponseFormat.read(json)

      val number = response.numbering.flatMap(_.cleansing.map(_.call).map{case Number(phone, code, _, _, _) =>
        s"$code$phone"
      }).getOrElse(throw TelesignInvalidPhoneNumber("E.164 phone number is not detected"))

      val riskLevel = response.risk.map{r =>
        RiskLevelEnum.withName(r.level)
      }.getOrElse(RiskLevelEnum.high)

      val phoneType = response.phoneType.flatMap{t =>
        Try(EnumPhoneTypes.withId(t.code.toInt)).toOption
      }.getOrElse(EnumPhoneTypes.Other)

      val carrier = response.carrier.map(_.name).getOrElse("Unknown")

      val timestamp = ZonedDateTime.parse(response.status.updatedOn, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

      TelesignPhoneScore(phone = number, riskLevel = riskLevel, phoneType = phoneType, carrier = carrier, updatedOn = timestamp)
    }.getOrElse(deserializationError(s"can't parse response into TelesignPhoneScore, raw response: ${json.compactPrint}"))
  }


  implicit val phoneScoreReader = new RootJsonReader[PhoneScore] {
    override def read(json: JsValue) = {
      val response: PhoneIdResponse = phoneIdResponseFormat.read(json)
      if (Status.isFailed(response.status)) {
        throw TelesignRequestFailure(response.status)
      } else {
        val country: String = response.location.flatMap(_.country.map(_.iso3)).getOrElse {
          throw TelesignInvalidPhoneNumber("ISO 3166-1 3-letter country code is not detected")
        }
        val phone: String =
          response
            .numbering
            .flatMap(_.cleansing.map {
              cleansing =>
                TelesignInvalidPhoneNumber(cleansing.call.cleansedCode) match {
                  case Some(ex) => throw ex
                  case None => s"${cleansing.call.countryCode}${cleansing.call.phoneNumber}"
                }
            }).getOrElse {
            throw TelesignInvalidPhoneNumber("E.164 phone number is not detected")
          }

        val _score: Int = response.risk.map(_.score).getOrElse(1000) // 1000 is the highest risk value, see http://docs.telesign.com/rest/content/xt/xt-score.html#xref-score
        val _phoneType = response
          .phoneType
          .map {
            case pt =>
              Try(EnumPhoneTypes.withId(pt.code.toInt)) match {
                case Success(ptt) =>
                  ptt
                case Failure(ex) =>
                  EnumPhoneTypes.Other
              }
          }.getOrElse(EnumPhoneTypes.Other)

        PhoneScore(
          phone = phone,
          country = country,
          phoneType = _phoneType,
          score = _score)
      }
    }
  }
}
