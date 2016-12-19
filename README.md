
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

Browse to http://localhost:9028/lookup-address/

## Operation Overview

In essence, four parties are involved:

 * a frontend service in the tax platform (the 'calling service' here)
 * the user-agent (i.e. web browser) and the user who operates it
 * the *address-lookup-frontend*, described here
 * the backend *address-lookup*, containing large national datasets of addresses.

*address-lookup-frontend* operates as a micro-site: the interaction sequence is as follows.

 1. The calling service sends the user-agent to *address-lookup-frontend* by means of HTTP redirection to the 'on-ramp'.
 2. The end-user chooses or edits an address, depending on use-case.
 3. The user-agent is redirected back to the calling service, along with an identifier.
 4. Using this identifier, the calling service obtains the chosen address directly via a REST resource provided in *address-lookup-frontend*.

In the last step, data exchange happens server-side, eliminating any possibility of user tampering.

## On-Ramp

For the use-case of entering UK addresses described below, the initial URL (a.k.a "on-ramp") is one of

```
http://localhost:9028/address-lookup-frontend/uk/addresses/<tag>
http://address-lookup-frontend.service/address-lookup-frontend/uk/addresses/<tag>
http://address-lookup-frontend.service/address-lookup-frontend/uk/addresses/<tag>?id=<id>&continue=<continue-url>
```

where the `tag`, described below, specifies the pre-agreed journey option, e.g. `j0`. The same tag is used throughout each user-journey and in outcome retrieval; the tag will be url-safe.

There are two optional query parameters to the on-ramp:

 * `id` allows the journey identifier to be supplied; otherwise, a GUID is generated and used instead.
 * `continue` specifies the URL for returning the user-agent to the calling service.

## Varied Journey Options

Different variants are provided; these vary the messaging presented to users and enable or disable some features. The variants are preconfigured and will be evolved as needed. The required option is indicated by a short tag (e.g. `pta2`) as a path parameter in the URL.

The current set of variants and their options are in [ViewConfig](https://github.com/hmrc/address-lookup-frontend/blob/master/app/address/ViewConfig.scala).

After the (short) user journey,the user-agent is redirected back to the calling service via the specified URL. A query parameter called `id` will be appended. If the on-ramp included an `id`, the same value will be returned.

## Obtaining the Outcome

After the user journey, the outcome will now be available to the calling service via a REST resource using the same id. 

```
http://address-lookup-frontend.service/address-lookup-frontend/outcome/<tag>/<id>
```

The tag is the same as used in the first request. This verifies that the end-user didn't switch to a different journey (a 404 would result in that case because the outcome identification would not match).

## Use Case 1: Entering a new address using postcode lookup

In this use-case, a user will choose an address by entering a postcode and optionally a first line, house name or house number. Journey options are described above and include allowing the user to edit the address, or requiring them to choose one of a permitted set of known addresses.

## Other Use Cases

These are still under development.

## TODO

* bug - no address found - problem with editing
* audit; include keystore guid
* user-entered address - add requestID or similar

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
