package my.deadvictoria.hoeapi.spi

import my.deadvictoria.hoeapi.exception.HoeApiException
import my.deadvictoria.hoeapi.models.Pem
import my.deadvictoria.hoeapi.models.PowerCutEvent

/**
 * Main contract for interacting with the Hoe API.
 * Provides methods to fetch data related to PEMs and power cuts.
 */
interface HoeApiClient {
    /**
     * Fetches all PEMs listed on the hoe site.
     * @return A list of [Pem] objects representing all PEMs listed on the Hoe site.
     * @throws HoeApiException if there is an error fetching or parsing the PEMs.
     */
    @Throws(HoeApiException::class)
    suspend fun fetchAllPems(): List<Pem>

    /**
     * Fetches all possible power cuts by PEM ID (both planned and unplanned)
     * which are currently listed on the hoe site.
     * @param pemId The ID of a PEM to fetch the power cuts for. The ID can be obtained from [fetchAllPems].
     * @return A list of [PowerCutEvent] objects representing all listed power cut events for
     * the specified PEM.
     * @throws HoeApiException if there is an error fetching or parsing the power cuts.
     * @see fetchAllPems
     */
    @Throws(HoeApiException::class)
    suspend fun fetchAllActualPowerCuts(pemId: String): List<PowerCutEvent>
}
