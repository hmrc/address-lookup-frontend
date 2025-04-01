package controllers.auth

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test._

class SignOutControllerSpec extends PlaySpec with GuiceOneAppPerTest {

  "SignOutController" should {

    "redirect to gov.uk and clear the session on signOut" in {
      val controller = new SignOutController(stubMessagesControllerComponents())
      val result = controller.signOut().apply(FakeRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("https://www.gov.uk")
      session(result).data mustBe empty
    }

  }

}