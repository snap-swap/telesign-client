package com.snapswap.telesign.utils

import java.time.format.DateTimeFormatter
import java.time.{Clock, ZonedDateTime}

object DateTimeHelper {
  val RFC2616: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

  def nowUTC(): ZonedDateTime =
    ZonedDateTime.now(Clock.systemUTC())
}
