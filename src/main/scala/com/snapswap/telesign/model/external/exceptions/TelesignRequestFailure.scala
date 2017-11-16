package com.snapswap.telesign.model.external.exceptions

import akka.http.scaladsl.model.StatusCode


case class TelesignRequestFailure(httpStatus: StatusCode, responseBody: String) extends TelesignError {
  require(!httpStatus.isSuccess())

  override def details: String =
    s"Http request wasn't successful ${httpStatus.intValue()}: ${httpStatus.defaultMessage()} response body: $responseBody"
}