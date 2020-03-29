package model

import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.data.validation.ValidationError
import play.api.libs.json._
import utils.TestConstants._

class JourneyConfigDefaultsSpec extends WordSpecLike with MustMatchers {

  "JourneyConfigDefaultsSpec" should {

    "for the English constants" must {

      def englishConstants(ukMode: Boolean) = JourneyConfigDefaults.EnglishConstants(ukMode)

      "correctly render the phaseBanner for a supplied feedbackUrl" in {
        englishConstants(ukMode = false).defaultPhaseBannerHtml("/foo") mustBe
          s"This is a new service – your <a href='/foo'>feedback</a> will help us to improve it."
      }
    }

    "for the Welsh constants" must {

      def welshConstants(ukMode: Boolean) = JourneyConfigDefaults.WelshConstants(ukMode)

      "correctly render the phaseBanner for a supplied feedbackUrl" in {
        welshConstants(ukMode = false).defaultPhaseBannerHtml("/foo") mustBe
          s"Mae hwn yn wasanaeth newydd – bydd eich <a href='/foo'>adborth</a> yn ein helpu i’w wella."
      }
    }
  }
}
