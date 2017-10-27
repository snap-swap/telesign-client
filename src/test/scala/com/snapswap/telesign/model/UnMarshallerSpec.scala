package com.snapswap.telesign.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.snapswap.telesign.model.external._
import com.snapswap.telesign.model.internal.ErrorResponse
import com.snapswap.telesign.unmarshaller.UnmarshallerVerify
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class UnMarshallerSpec extends FlatSpec with Matchers {
  import UnmarshallerVerify._

  "Unmarshaller" should "be able to parse PhoneScore from a success response" in {
    val score = phoneIdScoreResponse.parseJson.convertTo[PhoneScore]
    score.phone shouldBe "79005557788"
    score.phoneType shouldBe EnumPhoneTypes.Mobile
    score.country shouldBe "RUS"
    score.score shouldBe 11
  }
  it should "be able to parse TelesignPhoneScore from a success response" in {
    val score = phoneIdScoreResponse.parseJson.convertTo[TelesignPhoneScore]
    score.phone shouldBe "79005557788"
    score.phoneType shouldBe EnumPhoneTypes.Mobile
    score.carrier shouldBe "T2 Mobile"
    score.riskLevel shouldBe RiskLevelEnum.low
    score.updatedOn shouldBe ZonedDateTime.parse("2017-10-18T09:48:41.019078Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }
  it should "be able to parse errors from a failure response" in {
    val errorResponse = phoneIdAuthorizationErrorResponse.parseJson.convertTo[ErrorResponse]
    errorResponse.errors shouldBe Seq(TelesignError(-30000, "Invalid Customer ID.."))
  }
  it should "fail with TelesignInvalidPhoneNumber the case of invalid phone number" in {
    val thrown = the [TelesignInvalidPhoneNumber] thrownBy invalidPhone.parseJson.convertTo[PhoneScore]
    thrown.getMessage shouldBe "The phone number appears to be formatted correctly, but cannot be matched to any specific area"
  }

  private val phoneIdScoreResponse =
    """{
      |  "numbering": {
      |    "original": {
      |      "complete_phone_number": "79005557788",
      |      "country_code": "7",
      |      "phone_number": "9005557788"
      |    },
      |    "cleansing": {
      |      "call": {
      |        "phone_number": "9005557788",
      |        "country_code": "7",
      |        "min_length": 10,
      |        "max_length": 10,
      |        "cleansed_code": 100
      |      },
      |      "sms": {
      |        "phone_number": "9005557788",
      |        "country_code": "7",
      |        "min_length": 10,
      |        "max_length": 10,
      |        "cleansed_code": 100
      |      }
      |    }
      |  },
      |  "location": {
      |    "coordinates": {
      |      "latitude": null,
      |      "longitude": null
      |    },
      |    "city": "Kaluga Region",
      |    "zip": null,
      |    "state": null,
      |    "metro_code": null,
      |    "country": {
      |      "name": "Russia",
      |      "iso2": "RU",
      |      "iso3": "RUS"
      |    },
      |    "county": null,
      |    "time_zone": {
      |      "name": null,
      |      "utc_offset_min": "+2",
      |      "utc_offset_max": "+2"
      |    }
      |  },
      |  "carrier": {
      |    "name": "T2 Mobile"
      |  },
      |  "risk": {
      |    "level": "low",
      |    "recommendation": "allow",
      |    "score": 11
      |  },
      |  "reference_id": "357CBB8A40D8051C9045B408AB0E0E2F",
      |  "phone_type": {
      |    "code": "2",
      |    "description": "MOBILE"
      |  },
      |  "status": {
      |    "updated_on": "2017-10-18T09:48:41.019078Z",
      |    "code": 300,
      |    "description": "Transaction successfully completed"
      |  },
      |  "external_id": null,
      |  "blocklisting": {
      |    "blocked": false,
      |    "block_code": 0,
      |    "block_description": "Not blocked"
      |  }
      |}""".stripMargin

  val phoneIdAuthorizationErrorResponse =
    """
      |{
      |   "status": {
      |      "updated_on": "2012-10-03T14:51:28.709526Z",
      |      "code": 501,
      |      "description": "Not authorized"
      |   },
      |   "signature_string": "",
      |   "errors": [
      |      {
      |         "code": -30000,
      |         "description": "Invalid Customer ID.."
      |      }
      |   ]
      |}
    """.stripMargin

  val phoneIdInvalidSignatureReponse =
    """
      |{
      |   "status": {
      |      "updated_on": "2012-10-03T14:51:28.709526Z",
      |      "code": 501,
      |      "description": "Not authorized"
      |   },
      |   "signature_string": "GET\n\nTue, 01 May 2012 10:09:16 -0700\nx-ts-nonce:dff0f33c-7b52-4b6a-a556-23e32ca11fe1\nv1/phoneid/standard/15555551234",
      |   "errors": [
      |      {
      |       "code": -30006,
      |       "description": "Invalid Signature."
      |      }
      |   ]
      |}
    """.stripMargin

  val invalidPhone =
    """{
      |	"reference_id": "354DE1F545D0101C904497FEBFAEC2B8",
      |	"resource_uri": null,
      |	"sub_resource": "score",
      |	"status": {
      |		"updated_on": "2016-03-15T04:16:32.289570Z",
      |		"code": 300,
      |		"description": "Transaction successfully completed"
      |	},
      |	"errors": [],
      |	"numbering": {
      |		"original": {
      |			"complete_phone_number": "3521234567",
      |			"country_code": "352",
      |			"phone_number": "1234567"
      |		},
      |		"cleansing": {
      |			"call": {
      |				"country_code": "352",
      |				"phone_number": "1234567",
      |				"cleansed_code": 103,
      |				"min_length": null,
      |				"max_length": null
      |			},
      |			"sms": {
      |				"country_code": "352",
      |				"phone_number": "1234567",
      |				"cleansed_code": 103,
      |				"min_length": null,
      |				"max_length": null
      |			}
      |		}
      |	},
      |	"phone_type": {
      |		"code": "8",
      |		"description": "INVALID"
      |	},
      |	"location": {
      |		"city": null,
      |		"state": null,
      |		"zip": null,
      |		"metro_code": null,
      |		"county": null,
      |		"country": {
      |			"name": "Luxembourg",
      |			"iso2": "LU",
      |			"iso3": "LUX"
      |		},
      |		"coordinates": {
      |			"latitude": null,
      |			"longitude": null
      |		},
      |		"time_zone": {
      |			"name": null,
      |			"utc_offset_min": null,
      |			"utc_offset_max": null
      |		}
      |	},
      |	"carrier": {
      |		"name": ""
      |	},
      |	"risk": {
      |		"level": "high",
      |		"recommendation": "block",
      |		"score": 959
      |	}
      |}""".stripMargin
}
