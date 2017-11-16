package com.snapswap.telesign.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.snapswap.telesign.model.external._
import com.snapswap.telesign.model.external.exceptions.{TelesignInvalidPhoneNumber, TelesignResponseFailure}
import com.snapswap.telesign.model.internal.CompletionDone
import com.snapswap.telesign.unmarshaller.UnmarshallerVerify
import org.scalatest.{Matchers, WordSpec}
import spray.json._

class UnMarshallerSpec extends WordSpec with Matchers {

  import UnmarshallerVerify._

  "Unmarshaller" when {
    "unmarshall into TelesignPhoneScore" should {
      "successfully unmarshall if there are no any errors" in {
        val score = PhoneScoreResponses.successful.parseJson.convertTo[TelesignPhoneScore]
        score.phone shouldBe "79005557788"
        score.phoneType shouldBe EnumPhoneTypes.Mobile
        score.carrier shouldBe "T2 Mobile"
        score.riskLevel shouldBe RiskLevelEnum.low
        score.riskScore shouldBe 11
        score.updatedOn shouldBe ZonedDateTime.parse("2017-10-18T09:48:41.019078Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      }
      "throw TelesignResponseFailure if status wasn't successful" in {
        val exception = the[TelesignResponseFailure] thrownBy PhoneScoreResponses.withBadStatus.parseJson.convertTo[TelesignPhoneScore]
        exception.getMessage should include("code wasn't successful")
      }
      "throw TelesignInvalidPhoneNumber if phone number has a bad cleansed_code" in {
        val exception = the[TelesignInvalidPhoneNumber] thrownBy PhoneScoreResponses.withBadPhone.parseJson.convertTo[TelesignPhoneScore]
        exception.getMessage should include("appears to be formatted correctly, but cannot be matched")
      }
      "throw TelesignInvalidPhoneNumber if phone number isn't provided" in {
        val exception = the[TelesignInvalidPhoneNumber] thrownBy PhoneScoreResponses.withoutPhone.parseJson.convertTo[TelesignPhoneScore]
        exception.getMessage should include("phone number is not detected")
      }
    }
    "unmarshall into PhoneVerificationId" should {
      "successfully unmarshall if there are no any exceptions" in {
        val referenceId = "357ECE05872C0C04904318C6812E0847"
        val vereficationResponse = VerifyResponses.successful(referenceId).parseJson.convertTo[PhoneVerificationId]
        vereficationResponse shouldBe PhoneVerificationId(referenceId)
      }
      "throw TelesignResponseFailure if status wasn't successful" in {
        val exception = the[TelesignResponseFailure] thrownBy VerifyResponses.withBadStatus.parseJson.convertTo[PhoneVerificationId]
        exception.getMessage should include("Internal Telesign code wasn't successful")
      }
      "throw TelesignResponseFailure if errors are containing in the response" in {
        val exception = the[TelesignResponseFailure] thrownBy VerifyResponses.withErrors.parseJson.convertTo[PhoneVerificationId]
        exception.getMessage should include("Some errors occurred in the telesign response")
      }
    }
    "unmarshall into CompletionDone" should {
      "successfully unmarshall if there are no any exceptions" in {
        val completionResponse = CompletionResponses.successful.parseJson.convertTo[CompletionDone]
        completionResponse shouldBe CompletionDone()
      }
      "throw TelesignResponseFailure if status wasn't successful" in {
        val exception = the[TelesignResponseFailure] thrownBy CompletionResponses.withBadStatus.parseJson.convertTo[CompletionDone]
        exception.getMessage should include("Internal Telesign code wasn't successful")
      }
      "throw TelesignResponseFailure if errors are containing in the response" in {
        val exception = the[TelesignResponseFailure] thrownBy CompletionResponses.withErrors.parseJson.convertTo[CompletionDone]
        exception.getMessage should include("Some errors occurred in the telesign response")
      }
    }
  }

}
