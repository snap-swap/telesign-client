package com.snapswap.telesign.model.external

object TelesignMessageTypeEnum extends Enumeration {
  type TelesignMessageType = Value

  val OTP, ARN, MKT = Value
}
