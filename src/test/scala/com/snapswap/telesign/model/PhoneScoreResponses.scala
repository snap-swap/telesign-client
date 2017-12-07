package com.snapswap.telesign.model

object PhoneScoreResponses {
  private def basicResponse(cleansedCode: Int, withPhone: Boolean, withCountry: Boolean) = {
    val phone = if (withPhone)
      s"""
         |   ,"cleansing": {
         |      "call": {
         |        "phone_number": "9005557788",
         |        "country_code": "7",
         |        "min_length": 10,
         |        "max_length": 10,
         |        "cleansed_code": $cleansedCode
         |      },
         |      "sms": {
         |        "phone_number": "9005557788",
         |        "country_code": "7",
         |        "min_length": 10,
         |        "max_length": 10,
         |        "cleansed_code": $cleansedCode
         |      }
         |    }
       """.stripMargin
    else
      ""

    val country = if (withCountry)
      """
        |    "country": {
        |      "name": "Russia",
        |      "iso2": "RU",
        |      "iso3": "RUS"
        |    },
        """.stripMargin
    else
      ""

    s"""{
       |  "numbering": {
       |    "original": {
       |      "complete_phone_number": "79005557788",
       |      "country_code": "7",
       |      "phone_number": "9005557788"
       |    }
       |    $phone
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
       |    $country
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
  }

  val successful: String = basicResponse(100, withPhone = true, withCountry = true)

  val withoutPhone: String = basicResponse(100, withPhone = false, withCountry = true)

  val withBadPhone: String = basicResponse(103, withPhone = true, withCountry = true)

  val withBadStatus: String =
    """{
      |    "reference_id": "B56C5CAC2964010889502ADC56641615",
      |    "status": {
      |       "code": 11003,
      |       "description": "Invalid value for parameter account_lifecycle_event.",
      |       "updated_on": "2017-03-28T23:05:48.398146Z"
      |		}
      |}""".stripMargin

  val withoutCountry: String = basicResponse(100, withPhone = true, withCountry = false)
}
