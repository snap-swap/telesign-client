package com.snapswap.telesign.model.external

import java.time.ZonedDateTime

import com.snapswap.telesign.model.external.RiskLevelEnum.RiskLevel

case class TelesignPhoneScore(phone: String,
                              riskLevel: RiskLevel,
                              phoneType: EnumPhoneTypes.PhoneType,
                              carrier: String,
                              updatedOn: ZonedDateTime)
