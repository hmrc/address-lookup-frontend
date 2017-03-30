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

[![Address Lookup Frontend journey](https://www.draw.io/?lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=Address%20Lookup%20Frontend#R7Vxbk%2BMmFv41rto8tEugqx%2BnPd3Jbs1mpqaTyu5TCkvIZkcSXkket%2FPrA%2BgOyFZ7JNuZ2A9uCSNAnO98nHM49Mxcxq8%2Fpmi7%2BTcNcDSDRvA6M9%2FPIFx4NvvmBYeiACyMRVGyTklQljUFL%2BQPXBYaZemOBDjrVMwpjXKy7Rb6NEmwn3fKUJrSfbdaSKNur1u0xkrBi48itfQ3EuSbotSDTlP%2BEybrTdUzcMr3i1FVuXyTbIMCum8VmU8zc5lSmhdX8esSR3zyqnmpnssP1WBm5uMmjyN2A9il%2BPm552Ew5GE2%2FBQnebu7vvZcw1lh07QXgRm6tvngFg18RdGubP8DpV92W6XblO6SAAdlt%2FsNyfHLFvn81z2DTHdU9RwZ7CakSf6MYhJx7CzpLiU4ZW3%2FjPfljyVaAK%2BMIrJO2I3P3odVYxVIFC1pRNn1%2B4QmmLeep%2FQLrgpn0HTEh%2F0SoGxTjzFCKxx9ohnJCe00%2BRWnOWHQ%2BCBVyOm29eu7ciQrmuc0roZaTggsp43Vxa8SwhpRgBoeTK8wjXGeHliV8gHHLJFR6pRX3u4bfJpGWbZpYRPaZSEqdWJdN90Inl2Ush%2BGA0%2FBwQuOuCLecTA1Dhb2DeFgoeDgKSB3FFyeDUx7KAysCWBQLdstHCxpEpI0vkPh4oRwWSgAC4dgtfJsjEITLhgWgKWAYQadiHX6uGIXa37x6ePLL3VhWpWyvkjC6YP1%2Fe5%2FTMIJPlS1WOGqqTcKqMBbQdXGEINLYGMvsHRA8uDKFEAaQby16CrxWqp4AdSI15lGuurSr4gDJ8E7boRzZYlQlhG%2FK4E3TTp%2BJfl%2FOAXM7fLuvyUh8OtPOCXsrbg%2BirJiLDhQzHtputl4Wb9%2BWctFpmP7hueawAE%2BCB56RNKaclsz41VZiiOUk6%2FdIejEUPbwiRI2uFriUJK4JUuyGHr5VNt6lxoClt1pCJhSQzlK1zhXGhKoqF%2F7TKBAdU1QaODHJz0LMPVnDcPnqHQubocCRlBoF3bFC9yrKnSlv98iJyyMvu9aStZ1aRcOWFRPSSkrXDTzWa21pVnu00C07D7OXNa%2Bg2Iuj%2BJbfYKthQXr1vWVjr8rPCzc29JaNRKDgiDFWaZKItKHaNh0bvmlf4gIE0J64wIAQHJ5gKFKwNEIwJtGAKrrO60ddJ5l088kxdp%2FHF%2F97OicNkqPWUjDjSFF72QbZrAxZEhjVoAxpTVkqqtsESkx%2FoHjbX74YVrwdBzat7sxGpf1YkCEeiCODjRGJxKmjbOR5p1oqQdpTN7o0Kq25RWyI7ohB4GsTjSfXRQt6p%2Fu%2BgZ2d4Q0DDM8gRqYihr8hJKAAUtZtZ6pLnq0ofFql934UrVYSHJxVHb0LrZSmbYy50X4ZVKvPWHjFm57dSO8duHDj0YgcAiBFJC7hZXMO7H%2BDOcX%2B5ormXNVOBVRoBpQFzeMTsFJMld0wdiR4CSRDFyMZhjJLU0KJ9WRebNj61d7Dd%2BVxwkAkLB0XZezIsaLR35BO%2B7b6P%2FEq0tlqpyggxN%2BkmwJaEQ4kvlay%2Beb%2BQCC8%2Fig33wdlTGsAYzRkAJX9eeisYItdBwhc8tTElyLOCTnLPR87Ps654xNiWVPRTU2uC7VXHqTaTTScIeQhnUzpGGY7pzNtr2wTNe1Qadby4Vzz2a0YjvsxlsszqQTT%2FaG5XGOZ17o9%2FE6OHpOmegfMFdwYxkRnKjZKlVgdBdH7%2FycHtvqrzf1RZLAI%2FK%2FrAVPtDVYfGZqQkCRJnAz1kZPLKGbRTANAShig4rYfsOMro1fMz4Jd3Gp4tLmfFxIWmpMZ1qy7skIqO%2BmzAk4Re1F8kuv2CyZC82pqN2SHVF4pjnoyGFRuaEJ6Vvd9BT0XbA3TxUiKGLAYDamUacLGUj8uscznoiRs9IZH7Hz%2Fx1PuX6kyUPK9zTrAk4rnz%2BorCLsrSr5Gw4y%2BQQNxa9rnhM%2FXyGG8rmgl8%2FYz5coiuiOdyiy5E0RledTCjhsswLe9qimYxhCvekYOCvH5nH9KksuwmHeS3ljsFXf1kvbvNTxlTsGX8nJihq6Ut2HhD4czUD7u7sMclqarYlxT%2BYxyCJV7YV%2F1XSw5ITxqrHyFAU%2FosxhRPf%2BBqX5HCUJzRG3J36HLQ1Kixf8BinKaaaSTsqmTEwCvm2iKm39wwhCtiW91aitdrUCE8hYlwBzS1bGsRC1uJnILIGqWaJVj34hS4EjMNkOCDSkfMRzd0BMCZdKQyPaJNJcqrsfjSnxkZsanyXrQsKo4KIOKlPMVn%2B0EhU4LsqwGattP87s9zNNMnuvzg%2Fwa5TV45hS9HJIefCuHPasPrbWRq9Wg1UYfmO27EOVCnWo7rstjLKTLb%2BKuqV6DTICPVR0JrPoF9VjxHJi9ZA9h8n2woDTJRbz3Ni3KWV9Kg1NRyxAZ5dOianzUNKf6H0ij%2Be4Y%2BzKjrHGnBxro0SCJeOuObSYpMsv6zzoAAvYc89sPl1IQteaA6f5uFI3EwJLtY6r1DPuOeDgr5R71qZCqHBhZYLVNxNsCw6K8MMek2uCCD6Ac9j%2BdFEnH%2FEaimYPuHO39fE6zVoDQ%2FhvTWhTNjGrKNawjDZ531Ia5CSWAFCjn0WU2iCZMG4CkoqD0UZOtUnydVyK15YjU4SX5gwiqfrsiv%2BbA108%2FB65GjFyJa1LD7qN0YtFrlQf%2BGMDGyPCKMh6gJYxDeUg3BYas%2BRvsCH%2BpsJpEz4N8JbRv3Zf7A6s8YBlw1sClurP%2FJMDK6YZB42PMh5j56DZFdy2J6K9wh%2BFxmpHooAka3aZoBiL8OlMRN27h4iM%2BpxRgdJWXL4%2B6teQX32g5Y7D6XAoJxtcF4dqdOWXjWAlH5UrIf9THmSDRpjSmA%2BBkx8RUKUhf%2FuUMqChiBMb5ULxN5RmGtBV%2BI1RsmMgOByJ29xBN%2Baqali3hDo1oax0kEJxBoHzHiYMe6nAFn7Y0u0uQjlzamoIKpATp3oujSLozV27BpJTeEjfOZZkArOrxewqUFIT19reAMq%2BCNAIGqqzlisDv%2BQ1UaNe%2B%2Fjw2NN5s%2BiucLEO82orwY09aUx3xhqRsaCUTwF1ydg6mMkRxFFgph4%2BXdbprp1shx0zt7hB1gCq7V0KfH1FJBJx%2FAqXJZwMtN1GbDLFhqPiUAhYtjoKQyWv4o7HC%2BLRglfEYxV1uoWjAUe3ZseKCw7YK%2BkLlN9A2q8nnx%2BT9%2BuHRgmVI21yQ9NFteG1sg7%2FWpCDRyHnyTt0QxM83g45OQPs3HRE5djbaOmIM55cWP1b1qJ6889tzac%2FAQ%3D%3D)]

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