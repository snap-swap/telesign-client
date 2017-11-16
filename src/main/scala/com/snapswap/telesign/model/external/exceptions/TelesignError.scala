package com.snapswap.telesign.model.external.exceptions

import scala.util.control.NoStackTrace

trait TelesignError extends NoStackTrace {
  def details: String

  override def getMessage: String = details
}


