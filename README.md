
# address-lookup-frontend

[![Build Status](https://travis-ci.org/hmrc/address-lookup-frontend.svg?branch=master)](https://travis-ci.org/hmrc/address-lookup-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/address-lookup-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/address-lookup-frontend/_latestVersion)

This microservice provides a user interface for entering and editing addresses. Assistance is provided to the end-user for looking up their address from a database (via the backend service *address-lookup*).

Initially, the use-case covers only UK addresses; BFPO addresses might be added soon.
The roadmap includes support for international addresses.

## Running the App

In dev mode:
```
sm --start ASSETS_FRONTEND -r 2.232.0
sm --start KEYSTORE        -r 8.5.0
sm --start ADDRESS_LOOKUP  -f
sm --start ADDRESS_LOOKUP_FRONTEND -f
nice sbt run
```

Browse to http://localhost:9000/address-lookup-frontend/

## Operation Overview

In essence, four parties are involved:

 * a frontend service in the tax platform (the 'calling service' here)
 * the user-agent (i.e. web browser) and the user who operates it
 * the *address-lookup-frontend*, described here
 * the backend *addreess-lookup*, containing large national datasets of addresses.

*address-lookup-frontend* operates as a micro-site: the interaction sequence is as follows.

 # The calling service sends the user-agent to *address-lookup-frontend* by means of HTTP redirection.
 # The end-user chooses an address.
 # The user-agent is redirected back to the calling service, along with an identifier.
 # Using this identifier, the calling service obtains the chosen address directly via a REST resource in *address-lookup-frontend*.

In the last step, data exchange happens server-side between, eliminating any possibility of user tampering.

## Varied Journey Options

Different variants are provided; these vary the messaging presented to users and enable or disable some features. The variants are preconfigured and can be evolved as needed. The required option is indicated by a short tag (e.g. `pta2`) as a path parameter in the URL. The same tag is used throughout each user-journey and in response retrieval.

For the use-case of entering UK addresses described below, the initial URL is one of

```
http://localhost:9025/address-lookup-frontend/uk/addresses/<tag>
http://address-lookup-frontend.service/address-lookup-frontend/uk/addresses/<tag>
http://address-lookup-frontend.service/address-lookup-frontend/uk/addresses/<tag>?guid=abc123&continue=https://www.gov.uk/personal-tax-account/change-address
```

where the final `tag` specifies the pre-agreed journey option, e.g. `j0`. (See [ViewConfig](https://github.com/hmrc/address-lookup-frontend/blob/master/app/address/uk/ViewConfig.scala).)

There are two optional query parameters:

 * `guid` allows the journey identifier to be supplied; otherwise, a GUID is generated and used instead.
 * `continue` specifies the URL for returning the user-agent to the calling service.

After the (short) user journey, an outcome will have been reached that is available to the calling service via a REST resource. 

```
http://address-lookup-frontend.service/address-lookup-frontend/uk/outcome/<tag>/<id>
```

where the `id` is supplied to the calling service via a query parameter called `id`.


## Use Case 1: Entering a new address using postcode lookup

In this use-case, a user will choose an address by entering a postcode and optionally a first line, house name or house number. The 

 * No request data is supplied via *keystore* in this case.
 * The calling app redirects the user's browser to *<address-lookup-frontend-endpoint>*`/uk/addresses/1?continue=https://myapp.gov.uk/return` (where 1 indicates that variant 1 is required, as described above, and the continue parameter supplies the return URL).
 * The user follows the *address-lookup-frontend* journey.
 * When complete, *address-lookup-frontend* writes data to *keystore* using the key `response1` in this example.
 * Then the browser is redirected to the continue URL supplied earlier and carries a *id* parameter to indicate where the response data can be found.
 * The calling app obtains the response data via *keystore*: `.../keystore/address-lookup/<id>/data/response<n>`.

## Other Use Cases

These are still under development.

## TODO

* bug - county fields get wiped
* bug - no address found - problem with editing
* audit; include keystore guid
* user-entered address - add requestID or similar
* vet the output page stays on the same profile as the previous pages
* outputs via keystore
* inputs via keystore (instead of, or as well as, profile?)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
