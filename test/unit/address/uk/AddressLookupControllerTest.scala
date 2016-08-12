/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package address.uk

import play.api.mvc.Results._

import concurrent._
import play.api.mvc._
import play.api.test._
import org.scalatestplus.play._
import play.api.data.Form
import play.api.data.Forms._
import play.api.test.Helpers._
import play.filters.csrf.CSRF

class AddressLookupControllerTest extends PlaySpec with Results with OneAppPerSuite {

  "addressLookup action" should {

    "check address template" in {
      val addressForm = Form[AddressForm] {
        mapping("continueUrl" -> optional(text),
          "house-name-number" -> optional(text),
          "postcode" -> text,
          "no-fixed-address" -> optional(text),
          "radio-inline-group" -> optional(text),
          "address-line1" -> optional(text),
          "address-line2" -> optional(text),
          "address-line3" -> optional(text),
          "town" -> optional(text),
          "county" -> optional(text)
        )(AddressForm.apply)(AddressForm.unapply)
      }

      val cfg = ViewConfig("Your address")
      val html = views.html.addressuk.blankForm(cfg, addressForm, Nil, false)(
        FakeRequest().withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken))
      contentAsString(html) must include("Your Address")
    }

//    "return a blank page" in {
//      val addressLookupService = mock[AddressLookupService]
//      val controller = new AddressLookupController(addressLookupService)
//      val request = FakeRequest().withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.getForm(None).apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Your Address")
//    }
  }

//  "addressLookupSelection action" should {
//    "check if 'edit this addr' is selected we return edit fields" in {
//      val controller = new AddressLookupController with DataWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?hiddenselection=hiddenselection&radio-inline-group=GB00001&UK-postcode=AA1AA1"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Address line 1")
//      bodyText must include("Address line 2")
//      bodyText must include("Nearest town or city")
//      bodyText must include("County")
//    }
//
//    "check if 'continue' is selected return a list of matching addresses" in {
//      val controller = new AddressLookupController with DataWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?UK-postcode=AA1AA1"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("AA1AA1")
//    }
//
//    "A postcode with >20 addresses will display an error message" in {
//      val controller = new AddressLookupController with Data60ItemsWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?UK-postcode=AA1AA1"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("More than 20 addresses found")
//    }
//
//    "A postcode with 'random' data will display an error message" in {
//      val controller = new AddressLookupController with DummyBadRequestWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?house-name-number=&UK-postcode=nfjewk"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("The postcode was unrecognised")
//    }
//
//    "check if 'continue' is selected we return edit fields" in {
//      val controller = new AddressLookupController with DummyWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?UK-postcode=AA1AA1"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText mustNot include("Address line 1")
//    }
//
//    "check if error if no postcode is entered 'continue'" in {
//      val controller = new AddressLookupController with DummyWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?UK-postcode="
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("A post code is required")
//    }
//
//    "display warning if no postcode entered" in {
//      val controller = new AddressLookupController with DummyWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText mustNot include("A post code is required")
//    }
//
//    "check valid address goes to completion page" in {
//      val controller = new AddressLookupController with DummyWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?UK-postcode=AA1AA1&UK-address-line1=line1"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Application complete")
//
//      bodyText must include("line1")
//    }
//
//    "check edit with line1 blank" in {
//      val controller = new AddressLookupController with DataWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?hiddenselection=hiddenselection&UK-postcode=AA1AA1&UK-address-line1="
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("""id="UK-postcode" name="UK-postcode" value=""""")
//    }
//
//
//
//    "check 'no fixed address'" in {
//      val controller = new AddressLookupController with DataWS
//      val request = FakeRequest(GET,
//        "/address-lookup-demo/address-lookup-selection?no-fixed-address=true&UK-postcode=''"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken).withFormUrlEncodedBody("no-fixed-address" -> "true")
//      val result = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Application complete")
//      bodyText must include("No fixed address")
//    }
//
//  }

//  "completion page is displayed" should {
//    "we press continue on the list of addrs" in {
//      val controller = new AddressLookupController with DummyWS
//      val request = FakeRequest(GET,
//        "http://localhost:9000/address-lookup-demo/address-lookup-selection?house-name-number=&UK-postcode=AA1AA1&radio-inline-group=GB00001"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Application complete")
//    }
//  }

//  "completion page with data filled in form DB is displayed" should {
//    "we press continue on the list of addrs" in {
//      val controller = new AddressLookupController with DummyListWS
//      val request = FakeRequest(GET,
//        "http://localhost:9000/address-lookup-demo/address-lookup-selection?house-name-number=&UK-postcode=AA1 AA1&radio-inline-group=GB00001"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.addressLookupSelection().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Application complete")
//    }
//  }

//  "international completion page" should {
//    "be displayed when we press continue" in {
//      val controller = new AddressLookupController with DummyWS
//      val request = FakeRequest(GET,
//        "http://localhost:9000/address-lookup-demo/address-lookup-int-selection?int-country=Cuba&int-address=AAA"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.intContinueButton().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Application complete")
//      bodyText must include("AAA")
//      bodyText must include("Country: Cuba")
//    }
//
//    "display error if no address typed" in {
//      val controller = new AddressLookupController with DummyWS
//      val request = FakeRequest(GET,
//        "http://localhost:9000/address-lookup-demo/address-lookup-int-selection?int-country=Cuba&int-address="
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.intContinueButton().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Address was left blank")
//    }
//
//  }


//  "bfpo addressLookup action" should {
//    "find address given a valid bfpo post code" in {
//      val controller = new AddressLookupController with DummyWSBFPOWithData
//      val request = FakeRequest(GET,
//        "http://localhost:9000/address-lookup-demo/address-lookup-bfpo-selection?hiddentab=bfpotab&BFPO-postcode=BF1+3AA"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.bfpoContinueButton().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("BFPO post code")
//      bodyText must include("BF1 3AA")
//      bodyText must include("Op Test")
//    }
//
//    "display confimation page given valid bfpo data" in {
//      val controller = new AddressLookupController with DummyWSBFPOWithData
//      val request = FakeRequest(GET,
//        "http://localhost:9000/address-lookup-demo/address-lookup-bfpo-edit?BFPO-postcode=BF1+3AA&BFPO-number=123&BFPO-service-number=456&BFPO-rank=yes&BFPO-name=bob&BFPO-unit-regiment-department=ikea&BFPO-operation-name=test"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.bfpoEditButton().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Application complete")
//
//      bodyText must include("Postcode: BF1 3AA")
//      bodyText must include("BFPO No: 123")
//      bodyText must include("Service No: 456")
//      bodyText must include("Name: yes, bob")
//      bodyText must include("Unit: ikea")
//      bodyText must include("Op name: test")
//    }
//
//    "edit address given a valid bfpo post code" in {
//      val controller = new AddressLookupController with DummyWSBFPOWithData
//      val request = FakeRequest(POST,
//        "http://localhost:9000/address-lookup-demo/address-lookup-bfpo-selection?hiddentab=bfpotab&BFPO-postcode=BF1+3AA"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.bfpoEditButton().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("BFPO post code")
//      bodyText must include("BF1 3AA")
//    }
//
//    "edit bfpo address given a invalid bfpo post code" in {
//      val controller = new AddressLookupController with DummyWSBFPOWithData
//      val request = FakeRequest(POST,
//        "http://localhost:9000/address-lookup-demo/address-lookup-bfpo-selection?BFPO-postcode="
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.bfpoContinueButton().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("Post code was left blank")
//    }
//
//    "edit address given a invalid bfpo post code" in {
//      val controller = new AddressLookupController with DummyWSBFPOWithInvalidPostCode
//      val request = FakeRequest(POST,
//        "http://localhost:9000/address-lookup-demo/address-lookup-bfpo-selection?hiddentab=bfpotab&BFPO-postcode=AA1 1ZZ"
//      ).withSession("csrfToken" -> CSRF.SignedTokenProvider.generateToken)
//      val result: Future[Result] = controller.bfpoContinueButton().apply(request)
//
//      val bodyText: String = contentAsString(result)
//      bodyText must include("BFPO post code")
//      bodyText must include("Invalid BFPO postcode found")
//      bodyText must include("AA1 1ZZ")
//    }
//
//
//  }
}



//trait DummyWS extends AddressLookupWS with DummyBfpoWS {
//  def findAddresses(postcode: String, filter: Option[String]): Future[Either[Status, Option[List[services.Address]]]] = {
//    Future.successful(Right(Some(List[services.Address]())))
//  }
//}
//
//trait DummyListWS extends AddressLookupWS with DummyBfpoWS {
//  def findAddresses(postcode: String, filter: Option[String]): Future[Either[Status, Option[List[services.Address]]]] = {
//    Future.successful(Right(Some(List[services.Address]( services.Address("GB00001",  Array[String]("line1","line2","line3"), "ATown", "AA1 AA1")))))
//  }
//}
//
//trait DataWS extends AddressLookupWS with DummyBfpoWS {
//  def findAddresses(postcode: String, filter: Option[String]): Future[Either[Status, Option[List[services.Address]]]] = {
//    Future.successful(Right(Some(List[services.Address](
//      services.Address("GB00001", Array[String](""), "ATown", "AA1AA1")
//    ))))
//  }
//}
//
//trait Data60ItemsWS extends AddressLookupWS with DummyBfpoWS {
//  val ListSize = 60
//
//  def findAddresses(postcode: String, filter: Option[String]): Future[Either[Status, Option[List[services.Address]]]] = {
//    Future.successful(Right(Some(List.fill[services.Address](ListSize) {
//      services.Address("GB00001", Array[String](""), "ATown", "AA1AA1")
//    })))
//  }
//}
//
//trait DummyBadRequestWS extends AddressLookupWS with DummyBfpoWS {
//  def findAddresses(postcode: String, filter: Option[String]): Future[Either[Status, Option[List[services.Address]]]] = {
//    Future.successful(Left(BadRequest))
//  }
//}
//
//
//
//trait DummyBfpoWS  extends BfpoLookupWS {
//  def findBfpo(postcode: String): Future[Either[Status, Option[List[BfpoDB]]]] = ???
//}
//
//
//trait DummyWSBFPOWithData extends AddressLookupWS with DummyBfpoWS {
//  def findAddresses(postcode: String, filter: Option[String]): Future[Either[Status, Option[List[services.Address]]]] = ???
//
//  override def findBfpo(postcode: String): Future[Either[Status, Option[List[BfpoDB]]]] = {
//    Future.successful(Right(Some(List[services.BfpoDB](BfpoDB(Some("Op Test"), "123", "BF1 3AA")))))
//  }
//}
//
//trait DummyWSBFPOWithInvalidPostCode extends AddressLookupWS with DummyBfpoWS {
//  def findAddresses(postcode: String, filter: Option[String]): Future[Either[Status, Option[List[services.Address]]]] = ???
//
//  override def findBfpo(postcode: String): Future[Either[Status, Option[List[BfpoDB]]]] = {
//    Future.successful(Left(BadRequest))
//  }
//}
