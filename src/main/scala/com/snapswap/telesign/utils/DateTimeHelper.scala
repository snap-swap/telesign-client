package com.snapswap.telesign.utils

import java.time.format.DateTimeFormatter
import java.time.{Clock, ZonedDateTime}

object DateTimeHelper {
  val PATTERN = DateTimeFormatter.RFC_1123_DATE_TIME

  def nowUTC(): ZonedDateTime =
    ZonedDateTime.now(Clock.systemUTC())
}
