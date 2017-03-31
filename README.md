# Address Lookup Frontend

[![Build Status](https://travis-ci.org/hmrc/address-lookup-frontend.svg)](https://travis-ci.org/hmrc/address-lookup-frontend-new) [ ![Download](https://api.bintray.com/packages/hmrc/releases/address-lookup-frontend-new/images/download.svg) ](https://bintray.com/hmrc/releases/address-lookup-frontend/_latestVersion)

This microservice provides a user interface for entering and editing addresses. Assistance is provided to the end-user for looking up their address from a database (via the backend service [address-lookup](https://github.com/hmrc/address-lookup)).

Initially, the use-case covers only UK addresses. BFPO addresses might be added soon. The roadmap includes support for international addresses.

## Functional Overview

### Summary

During the utilization of `address-lookup-frontend`, four parties are involved:

* A frontend service in the tax platform (the **"calling service"** here).
* The user-agent (i.e. web browser) and the user who operates it (the **"user"** here).
* The `address-lookup-frontend` (the **"frontend** here).
* The backend `address-lookup`, containing large national datasets of addresses (the **"backend"** here).

The integration process from the perspective of the **calling service** consists of the following steps:

* [Configure](#Configuring a journey) your journey. You can use the default `j0` journey but some customization is, generally speaking, required in practice.
* Issue a request to `POST /lookup-address/init/:journeyName`. The return body is a string which is the **"on ramp"** URL to which the **"user"** should be redirected.
* Redirect the **"user"** to the **"on ramp"** URL.
* The **"user"** completes the journey, following which they will be redirected to the **"off ramp"** URL (which is configured as part of the journey) with an appended `id=:addressId` URL parameter.
* Using the value of the `id` parameter, you can retrieve the user's confirmed address as JSON by issuing a request to `GET /lookup-address/confirmed?id=:addressId`. 

![Address Lookup Frontend journey](https://raw.githubusercontent.com/hmrc/address-lookup-frontend/master/docs/design.png)

### Configuring a Journey

The `address-lookup-frontend` allows the **"calling service"** to customize many aspects of the **"user's"** journey and the appearance of the **"frontend"** user interface. It is recommended that, for any given address capture scenario that the **"calling servie"** has, a dedicated **"journey configuration"** is created.

#### Configuration Format

The available configuration options are shown below along with their default values and explanatory comments (where necessary):

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
          # whether or not to show a phase banner (if true AND alphaPhase = false, shows "beta")
          showPhaseBanner = false
          # if showPhaseBanner = true AND this = true, will show "alpha" phase banner
          alphaPhase = false
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
          }
          # configuration of the "confirm" page, in which the user is requested to confirm a "finalized" form for their address
          confirmPage {
            # the html->head->title text
            title = "Confirm Address"
            # the main heading to display on the page
            heading = "Confirm Address"
            # a subheading to display above the "finalized" address
            infoSubheading = "Your selected address"
            # an explanatory message to display below the subheading to clarify what we are asking of the user
            infoMessage = "This is how your address will look. Please double-check it and, if accurate, click on the <kbd>Confirm</kbd> button."
            # the submit button text (will result in them being redirected to the "off ramp" URL (see continueUrl)
            submitLabel = "Confirm"
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
          }
        }
      }
    }
  }
}
```

Additional configuration options may be introduced in future. However, the design intent is that **all** configuration options should **always** have a default value. Consequently, **"calling services"** should only ever need to provide overrides to specific keys, rather than re-configuring or duplicating the entire journey for each scenario.

Within the context of `app-config-*`, you would need to override individual keys by specifying the full HOCON path; e.g. `microservice.services.address-lookup-frontend.journeys.yourJourneyName.continueUrl: /some/other/place`. 

#### Delivery of Configuration

Additional custom configuration should be provided by adding entries to `address-lookup-frontend.yaml` in the appropriate global or environment-specific `app-config-*` repository.

*Please note: changes and additions should be introduced via pull requests which are merged by a member of the TxM North team as it will need to be tested and released.*

### Initializing a Journey

TODO

### Obtaining the Confirmed Address

TODO

### Running the Application

TODO

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")