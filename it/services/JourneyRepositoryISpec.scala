package services

import itutil.IntegrationSpecBase
import itutil.config.IntegrationTestConstants._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyRepositoryISpec extends IntegrationSpecBase {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val journeyRepository: KeystoreJourneyRepository = app.injector.instanceOf[KeystoreJourneyRepository]

  "getV2" should {
    "return a v2 model" when {
      "a v2 model is stored" in {
        stubKeystore(testJourneyId, journeyDataV2FullJson)
        await(journeyRepository.getV2(testJourneyId)) shouldBe Some(journeyDataV2Full)
      }
    }
    "return None" when {
      "there is no stored data" in {
        stubKeystore(testJourneyId, Json.obj(), 404)
        await(journeyRepository.getV2(testJourneyId)) shouldBe None
      }
    }
  }

  "putV2" should {
    "store a v2 model" in {
      stubKeystoreSave(testJourneyId, journeyDataV2FullJson, 200)
      await(journeyRepository.putV2(testJourneyId, journeyDataV2Full)) shouldBe true
    }
  }

}
