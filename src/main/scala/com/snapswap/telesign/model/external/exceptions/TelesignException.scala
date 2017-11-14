package com.snapswap.telesign.model.external.exceptions

import scala.util.control.NoStackTrace

trait TelesignException extends NoStackTrace {
  def details: String

  override def getMessage: String = details
}


