package com.snapswap.telesign.unmarshaller

trait UnmarshallerVerify
  extends CommonJsonSupport
    with VerifyResponseUnmarshaller
    with PhoneIdResponseUnmarshaller

object UnmarshallerVerify extends UnmarshallerVerify