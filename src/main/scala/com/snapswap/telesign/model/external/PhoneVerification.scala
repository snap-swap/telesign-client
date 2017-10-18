package com.snapswap.telesign.model.external

case class PhoneVerification(id: PhoneVerificationId,
                             errors: Seq[TelesignError],
                             status: EnumSmsStatusCodes.SmsStatusCode)