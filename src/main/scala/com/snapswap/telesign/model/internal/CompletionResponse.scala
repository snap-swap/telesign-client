package com.snapswap.telesign.model.internal

case class CompletionDone()

case class CompletionResponse(referenceId: Option[String],
                              resourceUri: Option[String],
                              subResource: Option[String],
                              errors: Seq[Error],
                              status: Status)
