package com.snapswap.telesign.model.external.exceptions

import com.snapswap.telesign.model.external.TelesignError
import com.snapswap.telesign.model.internal.Status
import spray.json.JsValue

case class TelesignResponseFailure(message: String, rawResponse: JsValue) extends TelesignException {

  override def details: String = s"$message, response body: ${rawResponse.compactPrint}"
}

object TelesignResponseFailure {
  def apply(failedStatus: Status, rawResponse: JsValue): TelesignResponseFailure = {
    require(Status.isFailed(failedStatus))

    val message = s"Internal Telesign code wasn't successful ${failedStatus.code}: ${failedStatus.description}"
    new TelesignResponseFailure(message, rawResponse)
  }

  def apply(errors: Seq[TelesignError], rawResponse: JsValue): TelesignResponseFailure = {
    require(errors.nonEmpty)

    val readableErrors = errors.map { e =>
      s"${e.code}: ${e.description}"
    }.mkString(";")
    val message = s"Some errors occurred in the telesign response: $readableErrors"
    new TelesignResponseFailure(message, rawResponse)
  }
}
