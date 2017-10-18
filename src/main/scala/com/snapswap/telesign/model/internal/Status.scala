package com.snapswap.telesign.model.internal

/**
  * An object containing details about the request status.
  * Both errors and successful codes can be here
  *
  * @param updatedOn   An ISO 8601 UTC timestamp indicating when the transaction status was updated.
  * @param code        One of the<emphasis> Transaction Status Codes</emphasis>.
  * @param description A description of the transaction status.
  */
private[telesign] case class Status(updatedOn: String,
                                    code: Int,
                                    description: String)

private[telesign] object Status {
  private val successfulCodes = Seq(300, 301)

  def isFailed(status: Status): Boolean =
    !successfulCodes.contains(status.code)
}