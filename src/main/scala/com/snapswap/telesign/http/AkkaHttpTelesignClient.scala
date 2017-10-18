package com.snapswap.telesign.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.Materializer
import com.snapswap.telesign._
import com.snapswap.telesign.model.external.AccountLifecycleEventEnum.TelesignAccountLifecycleEvent
import com.snapswap.telesign.model.external._
import com.snapswap.telesign.model.internal.{SmsVerifyResponse, VerifyResponse}
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
                                accountEmail: Option[String] = None): Future[PhoneScore] = {
    val params = Seq(
      Option("account_lifecycle_event" -> accountLifecycleEvent.toString),
      originatingIP.map("originating_ip" -> _.value),
      deviceId.map("device_id" -> _),
      accountId.map("account_id" -> _),
      accountEmail.map("email_address" -> _)
    ).flatten.toMap

    send(post(s"/score/$phoneNumber", params)){raw =>
      raw.parseJson.convertTo[PhoneScore]
    }
  }

  override def initiateVerification(number: String, code: String): Future[PhoneVerificationId] =
    send(
      post(s"/verify/sms",
        Map(
          "phone_number" -> number,
          "language" -> "en-US",
          "template" -> code
        )
      )
    ) { responseStr =>
      val response = responseStr.parseJson.convertTo[SmsVerifyResponse]

      PhoneVerificationId(response.referenceId)
    }

  override def getVerification(id: PhoneVerificationId): Future[PhoneVerification] =
    send(get(s"/verify/${id.value}")) { responseStr =>
      val response = responseStr.parseJson.convertTo[VerifyResponse]


      PhoneVerification(id,
        response.errors,
        EnumSmsStatusCodes.withCode(response.status.code)
      )
    }

}
