package my.deadvictoria.hoeapi.spi

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import my.deadvictoria.hoeapi.impl.HoeApiClientImpl

object HoeApiClientFactory {

    fun create(): HoeApiClient {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        return HoeApiClientImpl(client)
    }
}
