
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
nice sbt run
```

Browse to http://localhost:9000/address-lookup-frontend/

## Varied Journeys

Different variants are provided for varying the messaging presented to users. These are preconfigured and can be evolved as needed. The required option is indicated by a zero-based integer as a path parameter in the URL. The same number is used again later in response retrieval.

For the use-case of entering UK addresses described below, the initial URL is

```
http://localhost:9000/address-lookup-frontend/uk/addresses/0
```

where the final 0 may be substituted by another option.

See [ViewConfig](https://github.com/hmrc/address-lookup-frontend/blob/master/app/address/uk/ViewConfig.scala).

## Data Transfer via Keystore

The calling app specifies what the *address-lookup-frontend* needs to do. This is choreographed by simple URL parameters and by data-exchange using the *keystore* microservice.

 * Request data may optionally be supplied to *address-lookup-frontend*, transferred via temporary *keystore* storage.
 * Similarly, response data will be returned by *address-lookup-frontend*, transferred back to the calling app via temporary *keystore* storage.

The rationale for using *keystore* in this way is to ensure easy transfer of data that is immune to external tampering.

Generally, our *keystore* paths will be as follows:

| Path                                   | Supported Methods | Description  |
| -------------------------------------- | ------------------| ------------ |
| `/keystore/:source/:id`                |        GET        |Returns the document the with the given `id` contained in the provided `source`|
| `/keystore/:source/:id/data/keys`      |        GET        |Returns all the keys contained in the document with the given `id` inside the provided source|
| `/keystore/:source/:id/data/:key       |        PUT        |Saves the mapping between the `key` and the Json body of the request in the `source` for the given `id`| 

For this application:

 # The `:source` parameter is always `address-lookup`.
 # The `:id` parameter is a guid (see below).
 # The `:key` parameter is either `request` or `response<n>`, where *n* is the same number used for the journey variant.
 # The data value is a list of addresses (as JSON), exchanged in either direction depending on use-cases journey. The format of the JSON is the same as used by *address-lookup*. 

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
