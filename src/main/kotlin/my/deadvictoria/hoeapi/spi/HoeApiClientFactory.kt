package my.deadvictoria.hoeapi.spi

import io.ktor.client.*
import my.deadvictoria.hoeapi.impl.HoeApiClientImpl

/**
 * Factory object for creating instances of [HoeApiClient].
 * Provides methods to create a default instance or an instance with a custom [HttpClient].
 */
object HoeApiClientFactory {

    /**
     * Creates a default instance of [HoeApiClient].
     * This instance is configured with default settings.
     *
     * @return A new instance of [HoeApiClient].
     */
    fun create(): HoeApiClient = HoeApiClientImpl()

    /**
     * Creates an instance of [HoeApiClient] with a custom [HttpClient].
     * This allows for customized configuration of the HTTP client used by the API.
     *
     * @param client The custom [HttpClient] to be used by the [HoeApiClient] instance.
     * @return A new instance of [HoeApiClient] configured with the provided [HttpClient].
     */
    fun createWithClient(client: HttpClient): HoeApiClient = HoeApiClientImpl(client)
}
