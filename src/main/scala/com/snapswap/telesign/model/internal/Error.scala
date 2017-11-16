package com.snapswap.telesign.model.internal

/**
  * An array of property-value pairs, that contain information on error conditions that might have resulted from the Request.
  *
  * @param code        A 1 to 5-digit error code (possibly negative) that indicates the type of error that occurred.
  * @param description A string that describes the type of error that occurred. If no error occurs, this parameter is empty.
  */
case class Error(code: Int, description: String)