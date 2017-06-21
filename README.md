# Address Lookup Frontend

[![Build Status](https://travis-ci.org/hmrc/address-lookup-frontend.svg)](https://travis-ci.org/hmrc/address-lookup-frontend-new) [ ![Download](https://api.bintray.com/packages/hmrc/releases/address-lookup-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/address-lookup-frontend/_latestVersion)

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

* Configure your journey. You can use the default `j0` journey but some customization is, generally speaking, required in practice.
* Issue a request to `POST /api/init/:journeyName`. You should receive a `202 Accepted` response with a `Location` header the value of which is the **"on ramp"** URL to which the **"user"** should be redirected.
* Redirect the **"user"** to the **"on ramp"** URL.
* The **"user"** completes the journey, following which they will be redirected to the **"off ramp"** URL (which is configured as part of the journey) with an appended `id=:addressId` URL parameter.
* Using the value of the `id` parameter, you can retrieve the user's confirmed address as JSON by issuing a request to `GET /api/confirmed?id=:addressId`. 

![Address Lookup Frontend journey](https://raw.githubusercontent.com/hmrc/address-lookup-frontend/master/docs/design.png)

### Configuring a Journey

The `address-lookup-frontend` allows the **"calling service"** to customize many aspects of the **"user's"** journey and the appearance of the **"frontend"** user interface. It is recommended that, for any given address capture scenario that the **"calling servie"** has, a dedicated **"journey configuration"** is created.

#### Configuration Format

The available configuration options are shown below along with their default values (or placeholder examples) and explanatory comments (where necessary):

```hocon
microservice {
  services {
    address-lookup-frontend {
      journeys {
        yourJourneyName {
          # the "off ramp" URL
          continueUrl = "/lookup-address/confirmed"
          # value of the link href attribute for the GDS "home" link
          homeNavHref = "http://www.hmrc.gov.uk/"
          # the main masthead heading text
          navTitle = "Address Lookup"
          # optional URL to provide additional CSS styling. Defaults to None.
          additionalStylesheetUrl = "/foo/bar/styles.css"
          # whether or not to show a phase banner (if true AND alphaPhase = false, shows "beta")
          showPhaseBanner = false
          # if showPhaseBanner = true AND this = true, will show "alpha" phase banner
          alphaPhase = false
          # Optional link to provide a user feedback link for phase banner.
          phaseFeedbackLink = "/help/alpha"
          # Optional text (allows HTML tags) for phase banner
          phaseBannerHtml = "This is a new service – your <a href='/help/alpha'>feedback</a> will help us to improve it."
          # whether or not to show back buttons on user journey wizard forms
          showBackButtons = false
          # whether or not to use HMRC branding
          includeHMRCBranding = true
          # Optional: name of your service in deskpro. Used when constructing the "report a problem" link. Defaults to None.
          deskProServiceName = "foo"
          # configuration of the "lookup" page, in which user searches for address using filter + postcode
          lookupPage {
            # the html->head->title text
            title = "Lookup Address"
            # the heading to display above the lookup form
            heading = "Your Address"
            # the input label for the "filter" field
            filterLabel = "Building name or number"
            # the input label for the "postcode" field
            postcodeLabel = "Postcode"
            # the submit button text (proceeds to the "select" page)
            submitLabel = "Find my address"
            # message to display in infobox above lookup form when no results were found
            noResultsFoundMessage = "Sorry, we couldn't find anything for that postcode."
            # message to display in infobox above lookup form when too many results were found (see selectPage.proposalListLimit)
            resultLimitExceededMessage = "There were too many results. Please add additional details to limit the number of results."
            # Text to use for link to manual address entry form
            manualAddressLinkText = "Enter address manually"
          }
          # configuration of the "select" page, in which user chooses an address from a list of search results
          selectPage {
            # the html->head->title text
            title = "Select Address"
            # the heading to display above the list of results
            heading = "Select Address"
            # the radio group label for the list of results
            proposalListLabel = "Please select one of the following addresses"
            # the submit button text (proceeds to the "confirm" page)
            submitLabel = "Next"
            # maximum number of results to display (when exceeded, will return user to "lookup" page)
            proposalListLimit = 50
            # Whether or not to show "search again" link back to lookup page
            showSearchAgainLink = false
            # Link text to use when 'showSearchAgainLink' is true
            searchAgainLinkText = "Search again"
            # Link text to use for the "edit adddress" link
            editAddressLinkText = "Edit address"
          }
          # configuration of the "confirm" page, in which the user is requested to confirm a "finalized" form for their address
          confirmPage {
            # the html->head->title text
            title = "Confirm Address"
            # the main heading to display on the page
            heading = "Confirm Address"
            # whether or not to show subheading and info message
            showSubheadingAndInfo = false
            # a subheading to display above the "finalized" address
            infoSubheading = "Your selected address"
            # an explanatory message to display below the subheading to clarify what we are asking of the user
            infoMessage = "This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."
            # the submit button text (will result in them being redirected to the "off ramp" URL (see continueUrl)
            submitLabel = "Confirm"
            # Whether or not to show "search again" link back to lookup page
            showSearchAgainLink = false
            # Link text to use when 'showSearchAgainLink' is true
            searchAgainLinkText = "Search again"
            # Link text to use for the "edit adddress" link
            changeLinkText = "Edit this address"
          }
          # configuration of the "edit" page, in which the user is permitted to manually enter or modify a selected address
          editPage {
            # the html->head->title text
            title = "Edit Address"
            # the heading to display above the edit form
            heading = "Edit Address"
            # the input label for the "line1" field (commonly expected to be street number and name); a REQUIRED field
            line1Label = "Line 1"
            # the input label for the "line2" field; an optional field
            line2Label = "Line 2"
            # the input label for the "line3" field; an optional field
            line3Label = "Line 3"
            # the input label for the "town" field; a REQUIRED field
            townLabel = "Town"
            # the input label for the "postcode" field; a REQUIRED field
            postcodeLabel = "Postcode"
            # the input label for the "country" drop-down; an optional field (defaults to UK)
            countryLabel = "Country"
            # the submit button text (proceeds to the "confirm" page)
            submitLabel = "Next"
            # Whether or not to show "search again" link back to lookup page
            showSearchAgainLink = false
            # Link text to use when 'showSearchAgainLink' is true
            searchAgainLinkText = "Search again"
          }
        }
      }
    }
  }
}
```

Additional configuration options may be introduced in future; for instance to prohibit "edit", to bypass "lookup", or to modify validation procedures for international or BFPO addresses. However, the design intent is that **all** configuration options should **always** have a default value. Consequently, **"calling services"** should only ever need to provide overrides to specific keys, rather than re-configuring or duplicating the entire journey for each scenario.

#### Delivery of Configuration

Additional custom configuration should be provided by modifying `application.conf` in this repository and issuing a pull request to TxM North.

*Please note: changes and additions should be introduced via pull requests which are merged by a member of the TxM North team as it will need to be tested and released.*

### Initializing a Journey

Once you have a configured, named journey, you need to **"initialize"** an instance in order to obtain an "on ramp" URL to which the **"user"** is then redirected. 

Initialization generates an ID which is utilized both to facilitate subsequent retrieval of the **"user's"** confirmed address *and* to prevent arbitrary access to and potential abuse of `address-lookup-frontend` by malicious **"users"**.

An endpoint is provided for initialization:

URL:

* `/api/init/:journeyName` where `journeyName` is the *configuration key* for the pre-configured journey you wish to initialize

Example URLs:

* `/api/init/j0`

Methods:

* `POST`

Headers:

* `User-Agent` (required): string

Message Body:

* None currently. Future intent is that you will be able to `POST` a JSON message with journey configuration details.

Status Codes:

* 202 Accepted: when initialization was successful
* 404 Not Found: when a configuration for the named journey could not be found
* 500 Internal Server Error: when, for any (hopefully transient) internal reason, the journey could not be initialized 

Response:

* No content
* The `Location` header will specify an **"on ramp"** URL, appropriate for the journey, to which the user should be redirected. Currently, the journey has a TTL of **60 minutes**.

### Obtaining the Confirmed Address

TODO

### Running the Application

TODO

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")