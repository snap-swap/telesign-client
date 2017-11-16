package com.snapswap.telesign.unmarshaller

trait UnmarshallerVerify
  extends CommonJsonSupport
    with VerifyResponseUnmarshaller
    with PhoneScoringResponseUnmarshaller
    with CompletionResponseUnmarshaller

object UnmarshallerVerify extends UnmarshallerVerify