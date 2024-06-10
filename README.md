[![](https://jitpack.io/v/mydeadvictoria/hoeapi.svg)](https://jitpack.io/#mydeadvictoria/hoeapi)

# HOE API client
This small library provides an unofficial API client for the HOE site.

# Usage
The library is published on Jitpack, so you can use it in the following way:
```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.mydeadvictoria:hoeapi:0.2.1")
}
```

# SPI
The library avoids throwing exceptions and instead returns a custom result type called `HoeApiResult`.
This sealed class represents a fallible operation and has two variants: `Ok` and `Error`.
The `Ok` variant indicates a successful operation, while the `Error` variant signifies
a failure and includes optional additional context to help diagnose the issue. Also,
an optional underlying exception is included.

To create a `HoeApiClient`, you must use the `HoeApiClientFactory` which creates
and preconfigures the client for you.

# Example
The minimal example which demonstrates typical usage.
```kotlin

import my.deadvictoria.hoeapi.spi.HoeApiClientFactory

suspend fun main() {
    val client = HoeApiClientFactory.create()
    val khm = client.searchSettlements("Хмельницький").getOrThrow()[0]
    val street = client.searchStreets("зарічанська", khm.id).getOrThrow()[0]
    val houseNumbers = client.fetchHouseNumbers(street.id).getOrThrow()
    val result = client.fetchPowerOutage(khm.id, street.id, houseNumbers.random())
    val outage: PowerOutageEvent = when (result) {
        is HoeApiResult.Ok -> result.value
        is HoeApiResult.Error -> {
            println("Failed to fetch power outage: $result")
            return
        }
    }

    when (outage) {
        is PowerOutageEvent.ActiveOutage -> println("Found an active outage: $outage")
        is PowerOutageEvent.NoOutage -> println("No outages found")
    }
}
```
Sample output for this code would look like this:
```shell
Found an active outage: ActiveOutage(settlementId=26499, streetId=280542, houseNumber=5/2, typeOfWork=Графік погодинних відключень, type=EMERGENCY, schedule=0, estimatedStartTime=2024-06-07T19:00, estimatedEndTime=2024-06-07T23:00)
```
