/*
 * Copyright 2017 HM Revenue & Customs
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
package itutil

import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.play.test.UnitSpec

trait IntegrationSpecBase extends UnitSpec with LoginStub
  with GivenWhenThen
  with OneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers
  with WireMockHelper with BeforeAndAfterEach with BeforeAndAfterAll with FakeAppConfig with PageContentHelper {

  val mockHost = WireMockHelper.wiremockHost
  val mockPort = WireMockHelper.wiremockPort

  override def beforeEach(): Unit = {
    resetWiremock()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }
}
