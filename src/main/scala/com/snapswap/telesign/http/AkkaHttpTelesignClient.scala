package com.snapswap.telesign.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.Materializer
import com.snapswap.telesign._
import com.snapswap.telesign.model.external.AccountLifecycleEventEnum.TelesignAccountLifecycleEvent
import com.snapswap.telesign.model.external._
import com.snapswap.telesign.unmarshaller.UnmarshallerVerify
import spray.json._

import scala.concurrent.Future

class AkkaHttpTelesignClient(override val customerId: String,
                             override val apiKey: String,
                             useCaseCode: EnumUseCaseCodes.UseCaseCode)
                            (implicit override val system: ActorSystem,
                             override val materializer: Materializer)
  extends TelesignClient
    with HttpMethods
    with UnmarshallerVerify {

  override val domain: String = "rest-ww.telesign.com"
  override val baseURL: String = "/v1"

  private val log = Logging(system, this.getClass)

  override def scorePhoneNumber(phoneNumber: String,
                                accountLifecycleEvent: TelesignAccountLifecycleEvent = AccountLifecycleEventEnum.update,
                                originatingIP: Option[IPAddress] = None,
                                deviceId: Option[String] = None,
                                accountId: Option[String] = None,
                                accountEmail: Option[String] = None): Future[TelesignPhoneScore] = {
    val params = Seq(
      Option("account_lifecycle_event" -> accountLifecycleEvent.toString),
      originatingIP.map("originating_ip" -> _.value),
      deviceId.map("device_id" -> _),
      accountId.map("account_id" -> _),
      accountEmail.map("email_address" -> _)
    ).flatten.toMap

    send(post(s"/score/$phoneNumber", params)) { raw =>
      raw.parseJson.convertTo[TelesignPhoneScore]
    }
  }

  override def sendMessage(phoneNumber: String,
                           verifyCode: Option[String], // if not defined, Telesign will generate a random code itself
                           template: String = "Your code is $$CODE$$",
                           senderId: Option[String] = None,
                           originatingIP: Option[IPAddress] = None): Future[PhoneVerificationId] = {
    val params = Seq(
      Option("phone_number" -> phoneNumber),
      verifyCode.map("verify_code" -> _),
      Option("template" -> template),
      Option("ucid" -> useCaseCode.toString),
      senderId.map("sender_id" -> _),
      originatingIP.map("originating_ip" -> _.value)
    ).flatten.toMap

    send(post("/verify/sms", params)) { raw =>
      raw.parseJson.convertTo[PhoneVerificationId]
    }
  }

}
