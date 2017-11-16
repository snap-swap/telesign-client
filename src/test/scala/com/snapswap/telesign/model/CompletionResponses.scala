package com.snapswap.telesign.model

object CompletionResponses {
  val successful: String =
    """{
      |  "resource_uri": "/v1/verify/357ECE05872C0C04904318C6812E0847",
      |  "errors": [],
      |  "reference_id": "357ECE05872C0C04904318C6812E0847",
      |  "status": {
      |    "updated_on": "2017-11-16T02:31:42.847410Z",
      |    "code": 1900,
      |    "description": "Verify completion successfully recorded"
      |  },
      |  "sub_resource": "sms"
      |}""".stripMargin

  val withBadStatus: String =
    """{
      |  "errors": [],
      |  "status": {
      |       "code": 500,
      |       "description": "Transaction Not Attempted",
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
      |    "code": 1900,
      |    "description": "Verify completion successfully recorded"
      |  }
      |}""".stripMargin
}
