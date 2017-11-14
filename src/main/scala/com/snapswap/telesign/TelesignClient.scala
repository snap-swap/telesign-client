package com.snapswap.telesign

import com.snapswap.telesign.model.external.AccountLifecycleEventEnum.TelesignAccountLifecycleEvent
import com.snapswap.telesign.model.external.{PhoneVerification, PhoneVerificationId, _}

import scala.concurrent.Future

trait TelesignClient {
  def scorePhoneNumber(phoneNumber: String,
                       accountLifecycleEvent: TelesignAccountLifecycleEvent = AccountLifecycleEventEnum.update,
                       originatingIP: Option[IPAddress] = None,
                       deviceId: Option[String] = None,
                       accountId: Option[String] = None,
                       accountEmail: Option[String] = None): Future[TelesignPhoneScore]

  def sendMessage(phoneNumber: String,
                  verifyCode: Option[String], // if not defined, Telesign will generate a random code itself
                  template: String = "Your code is $$CODE$$",
                  senderId: Option[String] = None,
                  originatingIP: Option[IPAddress] = None): Future[PhoneVerificationId]

  def initiateVerification(number: String, code: String): Future[PhoneVerificationId]

  def getVerification(id: PhoneVerificationId): Future[PhoneVerification]
}