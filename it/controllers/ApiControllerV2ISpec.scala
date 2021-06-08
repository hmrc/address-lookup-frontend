package controllers

import com.codahale.metrics.SharedMetricRegistries
import controllers.api.ApiController
import itutil.IntegrationSpecBase
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Environment, Mode}
import services.IdGenerationService
import model._
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.Json
import itutil.config.IntegrationTestConstants._

class ApiControllerV2ISpec extends IntegrationSpecBase {

  val testJourneyFromConfig = JourneyDataV2(
    config = JourneyConfigV2(
      version = testApiVersion,
      options = JourneyOptions(
        continueUrl = "/api/confirmed",
        homeNavHref = Some("http://www.hmrc.gov.uk/"),
        showPhaseBanner = Some(false),
        alphaPhase = Some(false),
        phaseFeedbackLink = Some("#"),
        showBackButtons = Some(true),
        includeHMRCBranding = Some(true),
        selectPageConfig = Some(SelectPageConfig(proposalListLimit = Some(50), showSearchAgainLink = Some(true))),
        confirmPageConfig = Some(ConfirmPageConfig(
          showSearchAgainLink = Some(true),
          showSubHeadingAndInfo = Some(false),
          showChangeLink = Some(true),
          showConfirmChangeText = Some(false)
        ))
      ),
      labels = Some(JourneyLabels(
        en = Some(LanguageLabels(
          appLevelLabels = Some(AppLevelLabels(navTitle = Some("Address Lookup"))),
          lookupPageLabels = Some(LookupPageLabels(
            title = Some("Lookup Address"),
            heading = Some("Your Address"),
            filterLabel = Some("Building name or number"),
            postcodeLabel = Some("Postcode"),
            submitLabel = Some("Find my address"),
            noResultsFoundMessage = Some("Sorry, we couldn't find anything for that postcode."),
            resultLimitExceededMessage = Some("There were too many results. Please add additional details to limit the number of results.")
          )),
          selectPageLabels = Some(SelectPageLabels(
            title = Some("Select Address"),
            heading = Some("Select Address"),
            proposalListLabel = Some("Please select one of the following addresses"),
            submitLabel = Some("Next"),
            searchAgainLinkText = Some("Search again")
          )),
          confirmPageLabels = Some(ConfirmPageLabels(
            title = Some("Confirm Address"),
            heading = Some("Confirm Address"),
            infoSubheading = Some("Your selected address"),
            infoMessage = Some("This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."),
            submitLabel = Some("Confirm"),
            searchAgainLinkText = Some("Search again")
          )),
          editPageLabels = Some(EditPageLabels(
            title = Some("Edit Address"),
            heading = Some("Edit Address"),
            line1Label = Some("Line 1"),
            line2Label = Some("Line 2"),
            line3Label = Some("Line 3"),
            townLabel = Some("Town"),
            postcodeLabel = Some("Postcode"),
            countryLabel = Some("Country"),
            submitLabel = Some("Next")
          ))
        ))
      ))
    )
  )

  object MockIdGenerationService extends IdGenerationService {
    override def uuid: String = testJourneyId
  }

  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()
    
    new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .bindings(bind[IdGenerationService].toInstance(MockIdGenerationService))
      .configure(fakeConfig())
      .build
  }

  lazy val addressLookupEndpoint = app.injector.instanceOf[ApiController].addressLookupEndpoint

  "/api/v2/init" when {
    "provided with valid JourneyDataV2 Json" should {
      "return ACCEPTED with a url in the Location header" in {
        val v2Model = JourneyDataV2(
          config = JourneyConfigV2(
            version = testApiVersion,
            options = JourneyOptions(
              continueUrl = testContinueUrl
            )
          )
        )

        stubKeystoreSave(testJourneyId, Json.toJson(v2Model), OK)

        val res = await(buildClientAPI("v2/init")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .post(Json.toJson(v2Model.config)))

        res.status shouldBe ACCEPTED
        res.header(HeaderNames.LOCATION) should contain(s"$addressLookupEndpoint/lookup-address/$testJourneyId/lookup")
      }
    }
  }

  "/api/v2/confirmed" when {
    "provided with a valid journey ID" should {
      "return OK with a confirmed address" in {
        val v2Model = testJourneyDataWithMinimalJourneyConfigV2.copy(confirmedAddress = Some(testConfirmedAddress))

        stubKeystore(testJourneyId, Json.toJson(v2Model), OK)

        val res = await(buildClientAPI(s"v2/confirmed?id=$testJourneyId")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get())

        res.status shouldBe OK
        Json.parse(res.body) shouldBe Json.toJson(testConfirmedAddress)
      }
    }

    "provided with an invalid journey ID" should {
      "return NOT FOUND" in {
        val v2Model = testJourneyDataWithMinimalJourneyConfigV2.copy(confirmedAddress = Some(testConfirmedAddress))

        stubKeystore(testJourneyId, Json.toJson(v2Model), OK)

        val res = await(buildClientAPI(s"v2/confirmed?id=1234")
          .withHttpHeaders(HeaderNames.COOKIE -> sessionCookieWithCSRF, "Csrf-Token" -> "nocheck")
          .get())

        res.status shouldBe NOT_FOUND
      }
    }
  }

}
