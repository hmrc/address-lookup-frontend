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

[![Address Lookup Frontend journey](docs/AddressLookupFrontend.svg)]

### Configuring a Journey

TODO

### Initializing a Journey

TODO

### Obtaining the Confirmed Address

TODO

### Running the Application

TODO

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")