package com.snapswap.telesign.unmarshaller

trait UnmarshallerVerify
  extends CommonJsonSupport
    with VerifyResponseUnmarshaller
    with PhoneScoringResponseUnmarshaller

object UnmarshallerVerify extends UnmarshallerVerify