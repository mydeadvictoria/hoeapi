package my.deadvictoria.hoeapi.spi

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import my.deadvictoria.hoeapi.impl.HoeApiClientImpl

/**
 * Factory object to create instances of HoeApiClient.
 */
object HoeApiClientFactory {

    /**
     * Creates a default HoeApiClient instance with pre-configured HttpClient.
     *
     * @return a new default instance of HoeApiClient
     */
    fun create(): HoeApiClient {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        return HoeApiClientImpl(client)
    }

    /**
     * Creates a HoeApiClient instance with a custom-configured HttpClient.
     * Keep in mind that the ContentNegotiation plugin is added before the custom configuration
     * as it is required.
     *
     * @param configure A lambda to configure the HttpClient instance.
     * @return a new instance of HoeApiClient
     *
     * Example usage:
     * ```
     * val customClient = HoeApiClientFactory.withCustomHttpClientConfig {
     *     // Add http timeouts support
     *     install(HttpTimeout) {
     *         requestTimeoutMillis = 15000
     *         connectTimeoutMillis = 10000
     *         socketTimeoutMillis = 20000
     *     }
     * }
     * ```
     */
    fun withCustomHttpClientConfig(configure: HttpClientConfig<CIOEngineConfig>.() -> Unit): HoeApiClient {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            configure()
        }
        return HoeApiClientImpl(client)
    }
}
