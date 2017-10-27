package com.snapswap.telesign.model.external

object RiskLevelEnum extends Enumeration {
  type RiskLevel = Value

  val high = Value("high")
  val mediumHigh = Value("medium-high")
  val medium = Value("medium")
  val mediumLow = Value("medium-low")
  val low = Value("low")
  val neutral = Value("neutral")
}
