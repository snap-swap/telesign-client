package com.snapswap.telesign.model.external

import java.time.ZonedDateTime

import com.snapswap.telesign.model.external.RiskLevelEnum.RiskLevel


case class TelesignPhoneScore(phone: String,
                              riskLevel: RiskLevel,
                              riskScore: Int,
                              phoneType: EnumPhoneTypes.PhoneType,
                              carrier: String,
                              updatedOn: ZonedDateTime,
                              iso3CountryCode: String) {
  override def toString: String =
    s"""
       |phone = $phone,
       |riskLevel = $riskLevel,
       |riskScore = $riskScore,
       |phoneType = $phoneType,
       |carrier = $carrier,
       |updatedOn = $updatedOn,
       |iso3CountryCode = $iso3CountryCode
     """.stripMargin
}
