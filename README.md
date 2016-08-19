
# address-lookup-frontend

[![Build Status](https://travis-ci.org/hmrc/address-lookup-frontend.svg?branch=master)](https://travis-ci.org/hmrc/address-lookup-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/address-lookup-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/address-lookup-frontend/_latestVersion)

This microservice provides a user interface for entering addresses. Assistance is provided
to the user for looking up their address from a database (via a backend service).

Initially, the use-case covers only UK addresses; BFPO addresses might be added soon.
The roadmap includes support for international addresses.

### GET SETUP
* Follow [this walkthrough](https://confluence.tools.tax.service.gov.uk/display/DTRG/04+Service+Manager+Setup)

### TODO

* bug - county fields get wiped
* bug - no address found - problem with editing
* audit; include keystore guid
* user-entered address - add requestID or similar
* vet the output page stays on the same profile as the previous pages
* outputs via keystore
* inputs via keystore (instead of, or as well as, profile?)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
