package com.snapswap.telesign.model

object VerifyResponses {
  def successful(ref: String): String =
    s"""{
      |  "verify": {
      |    "code_state": "UNKNOWN",
      |    "code_entered": ""
      |  },
      |  "resource_uri": "/v1/verify/357ECE05872C0C04904318C6812E0847",
      |  "errors": [],
      |  "reference_id": "$ref",
      |  "status": {
      |    "updated_on": "2017-11-13T03:52:01.785063Z",
      |    "code": 290,
      |    "description": "Message in progress"
      |  },
      |  "sub_resource": "sms"
      |}""".stripMargin

  val withBadStatus: String =
    """{
      |  "errors": [],
      |  "status": {
      |       "code": 11003,
      |       "description": "Invalid value for parameter account_lifecycle_event.",
      |       "updated_on": "2017-03-28T23:05:48.398146Z"
      |  }
      |}""".stripMargin

  val withErrors: String =
    """{
      |  "errors": [
      |     {
      |         "code": -10001,
      |         "description": "Invalid parameter passed"
      |     }
      |  ],
      |  "status": {
      |    "updated_on": "2017-11-13T03:52:01.785063Z",
      |    "code": 290,
      |    "description": "Message in progress"
      |  }
      |}""".stripMargin
}
