package utils

import java.util.UUID

import model._

object TestConstants {

  val testAppLevelJourneyConfigV2 =
    JourneyDataV2(
      config = JourneyConfigV2(
        version = 2,
        options = JourneyOptions(
          continueUrl = "testContinueUrl",
          homeNavHref = Some("testNavHref"),
          additionalStylesheetUrl = Some("testStylesheetUrl"),
          phaseFeedbackLink = None,
          deskProServiceName = Some("testDeskproName"),
          showPhaseBanner = Some(true),
          alphaPhase = Some(true),
          showBackButtons = None,
          includeHMRCBranding = Some(true),
          ukMode = None,
          allowedCountryCodes = None,
          selectPageConfig = None,
          confirmPageConfig = None,
          timeoutConfig = Some(TimeoutConfig(
            timeoutAmount = 120,
            timeoutUrl = "testTimeoutUrl"
          ))
        ),
        labels = Some(JourneyLabels(
          en = Some(LanguageLabels(
            appLevelLabels = Some(AppLevelLabels(
              navTitle = Some("enNavTitle"),
              phaseBannerHtml = Some("enPhaseBannerHtml")
            )),
            selectPageLabels = None,
            lookupPageLabels = None,
            editPageLabels = None,
            confirmPageLabels = None
          )),
          cy = None
        ))
      )
    )

}
