
# address-lookup-frontend

[![Build Status](https://travis-ci.org/hmrc/address-lookup-frontend.svg?branch=master)](https://travis-ci.org/hmrc/address-lookup-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/address-lookup-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/address-lookup-frontend/_latestVersion)

This microservice provides a user interface for entering addresses. Assistance is provided
to the user for looking up their address from a database (via a backend service).

Initially, the use-case covers only UK addresses; BFPO addresses might be added soon.
The roadmap includes support for international addresses.

## Running the App

In dev mode:
```
sm --start ASSETS_FRONTEND -r 2.232.0
nice sbt run
```

Browse to http://localhost:9000/address-lookup-frontend/

## Varied Journeys

Different use-cases are supported and these can be evolved as needed.
See [ViewConfig](https://github.com/hmrc/address-lookup-frontend/blob/master/app/address/uk/ViewConfig.scala).

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
