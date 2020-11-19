# Address Lookup Frontend

[![Build Status](https://travis-ci.org/hmrc/address-lookup-frontend.svg)](https://travis-ci.org/hmrc/address-lookup-frontend-new) [ ![Download](https://api.bintray.com/packages/hmrc/releases/address-lookup-frontend/images/download.svg)<!-- @IGNORE PREVIOUS: link --> ](https://bintray.com/hmrc/releases/address-lookup-frontend/_latestVersion)<!-- @IGNORE PREVIOUS: link -->

This microservice provides a user interface for entering and editing addresses. Assistance is provided to the end-user for looking up their address from a database (via the backend service [address-lookup](https://github.com/hmrc/address-lookup)).

Initially, the use-case covers only UK addresses. BFPO addresses might be added soon. The roadmap includes support for international addresses.

## Functional Overview

### Summary

During the utilization of `address-lookup-frontend`, four parties are involved:

* A frontend service in the tax platform (the **"calling service"** here).
* The user-agent (i.e. web browser) and the user who operates it (the **"user"** here).
* The `address-lookup-frontend` (the **"frontend"** here).
* The backend `address-lookup`, containing large national datasets of addresses (the **"backend"** here).

The integration process from the perspective of the **calling service** consists of the following steps:

* _Initialize_ a **journey** by issuing a request to `POST /api/init` where the message body is a **journey configuration** JSON message (see below). You should receive a `202 Accepted` response with a `Location` header the value of which is the **"on ramp"** URL to which the **"user"** should be redirected.
* _Redirect_ the **"user"** to the **"on ramp"** URL.
* The **"user"** completes the journey, following which they will be redirected to the **"off ramp"** URL (which is configured as part of the journey) with an appended `id=:addressId` URL parameter.
* Using the value of the `id` parameter, you can retrieve the user's confirmed address as JSON by issuing a request to `GET /api/confirmed?id=:addressId`. 

### Initializing a Journey

The first action by any **calling service** must be to **"initialize"** an address lookup **journey** in order to obtain an "on ramp" URL to which the **"user"** is then redirected. 

Initialization generates an ID which is utilized both to facilitate subsequent retrieval of the **"user's"** confirmed address *and* to prevent arbitrary access to and potential abuse of `address-lookup-frontend` by malicious **"users"**.

An endpoint is provided for initialization:

URL:

* `/api/init`
* (`/api/v2/init` is also supported, but clients are encouraged to use the versionless endpoint.)

Methods:

* `POST`

Message Body:

* A **journey configuration** message in `application/json` format (see details of the JSON format below)

Status Codes:

* 202 Accepted: when initialization was successful
* 500 Internal Server Error: when, for any (hopefully transient) internal reason, the journey could not be initialized 

Response:

* No content
* The `Location` header will specify an **"on ramp"** URL, appropriate for the journey, to which the user should be redirected. Currently, the journey has a TTL of **60 minutes**.

### Configuring a Journey

The `address-lookup-frontend` allows the **"calling service"** to customize many aspects of the **"user's"** journey and the appearance of the **"frontend"** user interface. Journey configuration is supplied as a JSON object in the body of the request to `POST /api/init` (see above).

It is **not** necessary to specify values for all configurable properties. _Only supply a value for properties where it is either required or you need to override the default_. Wherever possible, sensible defaults have been provided. The default values are indicated in the table detailing the options below.

#### Configuration JSON Format

Journey configuration is supplied as a JSON object in the body of the request to `POST /api/init`.

It is **not** necessary to specify values for all configurable properties. _Only supply a value for properties where it is either required or you need to override the default_. Wherever possible, sensible defaults have been provided. The default values are indicated in the table detailing the options below.

**Welsh translations are enabled by default.**

You can provide custom labels for the Welsh journey by adding a `cy` block to the config. If no custom content is provided, the default labels are used. Welsh content is displayed for users when the `PLAY_LANG` cookie is set to `"cy"`. A language toggle to enable users to change their language will be displayed on all pages.

If your service doesn't have Welsh translations you can disable them setting the `disableTranslations` option to `true`. The language toggle will not be display and the PLAY_LANG cookie will be ignored.

```json
{
  "version": 2,
  "options": {
    "continueUrl": "...",
    "homeNavHref": "..",
    "signOutHref": "..",
    "accessibilityFooterUrl": "...",
    "phaseFeedbackLink": "/help/alpha",
    "deskProServiceName": "",
    "showPhaseBanner": false,
    "alphaPhase": false,
    "disableTranslations": true,
    "showBackButtons": false,
    "includeHMRCBranding": true,
    "allowedCountryCodes": [
      "GB",
      "FR"
    ],
    "ukMode": false,
    "selectPageConfig": {
      "proposalListLimit": 30,
      "showSearchLinkAgain": true
    },
    "confirmPageConfig": {
      "showChangeLink": false,
      "showSubHeadingAndInfo": false,
      "showSearchAgainLink": false,
      "showConfirmChangeText": true
    },
    "timeoutConfig": {
      "timeoutAmount": 900,
      "timeoutUrl": "http://service/timeout-uri",
      "timeoutKeepAliveUrl": "http://service/keep-alive-uri"
    }
  },
  "labels": {
    "en": {
      "appLevelLabels": {
        "navTitle": "",
        "phaseBannerHtml": ""
      },
      "selectPageLabels": {
        "title": "Choose address",
        "heading": "Choose address",
        "headingWithPostcode": "foo",
        "proposalListLabel": "Please select one of the following addresses",
        "submitLabel": "Continue",
        "searchAgainLinkText": "Search again",
        "editAddressLinkText": "Enter address manually"
      },
      "lookupPageLabels": {
        "title": "Find address",
        "heading": "Find address",
        "filterLabel": "Property name or number (optional)",
        "postcodeLabel": "Postcode",
        "submitLabel": "Find address",
        "noResultsFoundMessage": "Sorry, we couldn't find anything for that postcode.",
        "resultLimitExceededMessage": "There were too many results. Please add additional details to limit the number of results.",
        "manualAddressLinkText": "Enter the address manually"
      },
      "confirmPageLabels": {
        "title": "Confirm address",
        "heading": "Review and confirm",
        "infoSubheading": "Your selected address",
        "infoMessage": "This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button.",
        "submitLabel": "Confirm Address",
        "searchAgainLinkText": "Search again",
        "changeLinkText": "Edit address",
        "confirmChangeText": "By confirming this change, you agree that the information you have given is complete and correct."
      },
      "editPageLabels": {
        "title": "Enter address",
        "heading": "Enter address",
        "line1Label": "Address line 1",
        "line2Label": "Address line 2 (optional)",
        "line3Label": "Address line 3 (optional)",
        "townLabel": "Town/City",
        "postcodeLabel": "Postcode (optional)",
        "countryLabel": "Country",
        "submitLabel": "Continue"
      }
    },
    "cy": {
      "appLevelLabels": {
        "navTitle": "",
        "phaseBannerHtml": ""
      },
      "selectPageLabels": {
        "title": "Choose address welsh",
        "heading": "Choose address welsh",
        "headingWithPostcode": "foo",
        "proposalListLabel": "Please select one of the following addresses welsh",
        "submitLabel": "Continue welsh",
        "searchAgainLinkText": "Search again welsh",
        "editAddressLinkText": "Enter address manually welsh"
      },
      "lookupPageLabels": {
        "title": "Find address welsh",
        "heading": "Find address welsh",
        "filterLabel": "Property name or number welsh (optional)",
        "postcodeLabel": "Postcode welsh",
        "submitLabel": "Find address welsh",
        "noResultsFoundMessage": "Sorry, we couldn't find anything for that postcode. welsh",
        "resultLimitExceededMessage": "There were too many results. Please add additional details to limit the number of results. welsh",
        "manualAddressLinkText": "Enter the address manually welsh"
      },
      "confirmPageLabels": {
        "title": "Confirm address welsh",
        "heading": "Review and confirm welsh",
        "infoSubheading": "Your selected address welsh",
        "infoMessage": "This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button. welsh",
        "submitLabel": "Confirm Address welsh",
        "searchAgainLinkText": "Search again welsh",
        "changeLinkText": "Edit address welsh",
        "confirmChangeText": "By confirming this change, you agree that the information you have given is complete and correct. welsh"
      },
      "editPageLabels": {
        "title": "Enter address welsh",
        "heading": "Enter address welsh",
        "line1Label": "Address line 1 welsh",
        "line2Label": "Address line 2 (optional) welsh",
        "line3Label": "Address line 3 (optional) welsh",
        "townLabel": "Town/City welsh",
        "postcodeLabel": "Postcode (optional) welsh",
        "countryLabel": "Country welsh",
        "submitLabel": "Continue welsh"
      }
    }
  }
}
```
#### Test Endpoint for journey setup
* `/lookup-address/test-only/v2/test-setup` (GET)

#### Top-level configuration JSON object

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`continueUrl`|the "off ramp" URL for a user journey|**Required**|String|N/A|
|`homeNavHref`|value of the link href attribute for the GDS "home" link|Optional|String|`"http://www.hmrc.gov.uk/"`|
|`signOutHref`|value of the link href attribute for the Sign out link|Optional|String|`None`|
|`accessibilityFooterUrl`|value of the link href attribute for the "Accessibility Statement" link in the footer|Optional|String|`None`|
|`phaseFeedbackLink`|link to provide a user feedback link for phase banner|Optional|String|`"/help/alpha"`|
|`deskProServiceName`|name of your service in DeskPro. Used when constructing the "report a problem" link. Defaults to None.|Optional|String|`None`|
|`showPhaseBanner`|whether or not to show a phase banner (if `showPhaseBanner == true && alphaPhase == false`, shows "beta")|Optional|Boolean|`false`|
|`alphaPhase`|if `showPhaseBanner = true && alphaPhase == true`, will show "alpha" phase banner|Optional|Boolean|`false`|
|`showBackButtons`|whether or not to show back buttons on user journey wizard forms|Optional|Boolean|`false`|
|`includeHMRCBranding`|whether or not to use HMRC branding|Optional|Boolean|`true`|
|`allowedCountryCodes`|country codes list allowed in manual edit dropdown|Optional|List of Strings|All countries|
|`ukMode`|enable uk only Lookup and Edit mode|Optional|Boolean|`None`|

#### Select page configuration JSON object

Configuration of the "select" page, in which user chooses an address from a list of search results. The select page configuration is a nested JSON object inside the journey configuration under the `selectPage` property.

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`proposalListLimit`|maximum number of results to display (when exceeded, will return user to "lookup" page)|Optional|Integer|`nothing`|
|`showSearchAgainLink`|Whether or not to show "search again" link back to lookup page|Optional|Boolean|`false`|

#### Confirm page configuration JSON object

Configuration of the "confirm" page, in which the user is requested to confirm a "finalized" form for their address. The confirm page configuration is a nested JSON object inside the journey configuration under the `confirmPage` property.

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`infoSubheading`|a subheading to display above the "finalized" address|Optional|String|`"Your selected address"`|
|`showSearchAgainLink`|Whether or not to show "search again" link back to lookup page|Optional|Boolean|`false`|
|`showChangeLink`|Whether or not to show "Edit address" link back to Edit page|Optional|Boolean|`true`|
|`showConfirmChangeText`|Whether or not to show "confirmChangeText" displayed above the submit button|Optional|Boolean|`false`|

#### Timeout Configuration JSON object (Optional)

Configuration of the timeout popup in which user is shown a popup allowing them to extend their session before it times out. The timeout configuration is a nested JSON object inside the journey configuration under the `timeout` property.

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`timeoutAmount`|the duration of session timeout in seconds (between 120 and 999999999 seconds)|Required|Int|N/A|
|`timeoutUrl`|the url to be redirected to on session timeout|Required|String|N/A|
|`timeoutKeepAliveUrl`|keep alive url to keep the session alive on calling service|Optional|String|N/A|

#### Top-level label JSON object

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`navTitle`|the main masthead heading text|Optional|String|`"Address Lookup"`|
|`phaseBannerHtml`|text (allows HTML tags) for phase banner|Optional|String|`"This is a new service – your <a href='/help/alpha'>feedback</a> will help us to improve it."`"

#### Lookup page label JSON object

Labels for the "lookup" page.

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`title`|the `html->head->title` text|Optional|String|`"Find the address"`|
|`heading`|the heading to display above the lookup form|Optional|String|`"Find the address"`|
|`filterLabel`|the input label for the "filter" field|Optional|String|`"Property name or number (optional)"`|
|`postcodeLabel`|the input label for the "postcode" field|Optional|String|`"UK postcode"`|
|`submitLabel`|the submit button text (proceeds to the "select" page)|Optional|String|`"Find address"`|
|`noResultsFoundMessage`|message to display in infobox above lookup form when no results were found|Optional|String|`"Sorry, we couldn't find anything for that postcode."`|
|`resultLimitExceededMessage`|message to display in infobox above lookup form when too many results were found (see selectPage.proposalListLimit)|Optional|String|`"There were too many results. Please add additional details to limit the number of results."`|
|`manualAddressLinkText`|Text to use for link to manual address entry form|Optional|String|`"Enter the address manually"`|

#### Select page label JSON object

Labels for the "select" page.

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`title`|the `html->head->title` text|Optional|String|`"Choose address"`|
|`heading`|the heading to display above the list of results|Optional|String|`"Choose address"`|
|`headingWithPostCode`|the heading to display above the list of results when a postcode is provided|Optional|String|`"Showing all results for [postcode]"`|
|`proposalListLabel`|the radio group label for the list of results|Optional|String|`"Please select one of the following addresses"`|
|`submitLabel`|the submit button text (proceeds to the "confirm" page)|Optional|String|`"Continue"`|
|`proposalListLimit`|maximum number of results to display (when exceeded, will return user to "lookup" page)|Optional|Integer|`nothing`|
|`searchAgainLinkText`|Link text to use when 'showSearchAgainLink' is true|Optional|String|`"Search again"`|
|`editAddressLinkText`|Link text to use for the "edit adddress" link|Optional|String|`"Enter the address manually"`|

#### Confirm page label JSON object

Labels for the "confirm" page.

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`title`|the html->head->title text|Optional|String|`"Confirm the address"`|
|`heading`|the main heading to display on the page|Optional|String|`"Review and confirm"`|
|`infoSubheading`|a subheading to display above the "finalized" address|Optional|String|`"Your selected address"`|
|`infoMessage`|an explanatory message to display below the subheading to clarify what we are asking of the user (accepts HTML)|Optional|String|`"This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."`|
|`submitLabel`|the submit button text (will result in them being redirected to the "off ramp" URL (see continueUrl)|Optional|String|`"Confirm Address"`|
|`searchAgainLinkText`|Link text to use when 'showSearchAgainLink' is true|Optional|String|`"Search again"`|
|`changeLinkText`|Link text to use for the "edit adddress" link|Optional|String|`"Change address"`|
|`showConfirmChangeText`|Whether or not to show "confirmChangeText" displayed above the submit button|Optional|Boolean|`false`|
|`confirmChangeText`|Text displayed above the submit button when 'showConfirmChangeText' is true|Optional|String|`"By confirming this change, you agree that the information you have given is complete and correct."`|

#### Edit page label JSON object

Labels for the "edit" page.

|Field name|Description|Optional/Required|Type|Default value|
|----------|-----------|-----------------|----|-------------|
|`title`|the html->head->title text|Optional|String|`"Enter the address"`|
|`heading`|the heading to display above the edit form|Optional|String|`"Enter the address"`|
|`line1Label`|the input label for the "line1" field (commonly expected to be street number and name); a REQUIRED field|Optional|String|`"Address line 1"`|
|`line2Label`|the input label for the "line2" field; an optional field|Optional|String|`"Address line 2 (optional)"`|
|`line3Label`|the input label for the "line3" field; an optional field|Optional|String|`"Address line 3 (optional)"`|
|`townLabel`|the input label for the "town" field; a REQUIRED field|Optional|String|`"Town/City"`|
|`postcodeLabel`|the input label for the "postcode" field; a REQUIRED field|Optional|String|`"Postal code (optional)"`|
|`countryLabel`|the input label for the "country" drop-down; an optional field (defaults to UK)|Optional|String|`"Country"`|
|`submitLabel`|the submit button text (proceeds to the "confirm" page)|Optional|String|`"Continue"`|

Additional configuration options may be introduced in future; for instance to prohibit "edit", to bypass "lookup", or to modify validation procedures for international or BFPO addresses. However, the design intent is that **all** configuration options should **always** have a default value. Consequently, **"calling services"** should only ever need to provide overrides to specific keys, rather than re-configuring or duplicating the entire journey for each scenario.

#### ukMode (Optional)

When enabled:

Lookup returns Only UK Addresses; 1 link on Lookup Page is overridden; Edit Address Mode removes option to change country (Defaults to United Kingdom) and omits postcode field.

### Obtaining the Confirmed Address

Once the user has completed the address lookup journey, they will be redirected to the **off ramp** URL specified in the **journey configuration**. An `id` parameter will be appended to the **off ramp** URL. **Calling services** may use the value of this parameter to obtain the **user's** confirmed address.

URL:

* `/api/confirmed`

Example URLs:

* `/api/confirmed?id=ID_VALUE_FROM_APPENDED_OFF_RAMP_URL_ID_PARAMETER_HERE`

Methods:

* `GET`

Message Body:

* None

Status Codes:

* 200 Ok: when a confirmed address was successfully obtained for the given `ID`
* 404 Not Found: when no confirmed address was found for the given `ID`
* 500 Internal Server Error: when, for any (hopefully transient) internal reason, the journey data corresponding to the ID could not be obtained 

Response:

* An `application/json` message which describes a **confirmed address** (see below)

#### Confirmed Address example JSON Format

```json
{
    "auditRef" : "bed4bd24-72da-42a7-9338-f43431b7ed72",
    "id" : "GB990091234524",
    "address" : {
        "lines" : [ "10 Other Place", "Some District", "Anytown" ],
        "postcode" : "ZZ1 1ZZ",
        "country" : {
            "code" : "GB",
            "name" : "United Kingdom"
        }
    }
}
```
**Note** that the `id` attribute will not be present if the selected address was entered manually, or was an edited search result.

### Running the Application

`sm --start ADDRESS_LOOKUP_SERVICES -f; sm --stop ADDRESS_LOOKUP_FRONTEND`

run with
`sbt "run 9028 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"` (when in the address-lookup-frontend folder)

go to [localhost:9028/lookup-address/test-only/v2/test-setup](http://localhost:9028/lookup-address/test-only/v2/test-setup)<!-- @IGNORE PREVIOUS: link --> if not running from another service

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")<!-- @IGNORE PREVIOUS: link -->
