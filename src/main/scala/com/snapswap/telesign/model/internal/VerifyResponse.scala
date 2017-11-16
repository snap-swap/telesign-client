package com.snapswap.telesign.model.internal


private[telesign] object EnumVerifyCodeState extends Enumeration {
  type VerifyCodeState = Value
  val VALID, INVALID, UNKNOWN = Value
}

/**
  * An object that describes the verification status.
  *
  * @param codeState   Indicates whether the verification code entered matches that which was sent. Possible values are VALID, INVALID, or UNKNOWN.
  *                    When the code entered matches the code sent, the response will be VALID.
  *                    When the code entered does not match the code sent, code_state will be INVALID.
  * @param codeEntered Always set to an empty string.
  */
private[telesign] case class Verify(codeState: EnumVerifyCodeState.VerifyCodeState,
                                    codeEntered: Option[String])

/**
  * The <strong>VerifyResponse</strong> class encapsulates all of the information returned from a call to either the <strong>Verify SMS</strong> web service, or to the <strong>Verify Call</strong>  web service.
  *
  * @param referenceId A String containing a <em>reference identifier</em> that uniquely identifies the Request message that initiated this Response.
  * @param resourceUri A String containing the URI for accesses the PhoneID resource.
  * @param subResource A String containing the name of the subresource that was accessed. For example, "standard".
  * @param errors      An array of [[Error]] objects.
  *                    Each Error object contains information about an error condition that might have resulted from the Request.
  * @param status      An object containing details about the request status.
  * @param verify      An object that describes the verification status.
  */
private[telesign] case class VerifyResponse(referenceId: Option[String],
                                            resourceUri: Option[String],
                                            subResource: Option[String],
                                            errors: Seq[Error],
                                            status: Status,
                                            verify: Option[Verify])

