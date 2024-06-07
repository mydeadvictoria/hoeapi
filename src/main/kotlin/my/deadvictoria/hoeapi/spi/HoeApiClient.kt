package my.deadvictoria.hoeapi.spi

import my.deadvictoria.hoeapi.models.HoeApiResult
import my.deadvictoria.hoeapi.models.PowerOutageEvent
import my.deadvictoria.hoeapi.models.Settlement
import my.deadvictoria.hoeapi.models.Street

/**
 * Main contract for interacting with the Hoe API.
 * Provides methods to fetch settlements, streets, house numbers and power outages.
 */
interface HoeApiClient {

    /**
     * Searches for settlements that match the given query string.
     *
     * @param query The search query string.
     * @return A [HoeApiResult] containing a list of matching [Settlement] objects.
     */
    suspend fun searchSettlements(query: String): HoeApiResult<List<Settlement>>

    /**
     * Searches for streets within a specified settlement that match the given query string.
     *
     * @param query The search query string.
     * @param settlementId The ID of the settlement to search within.
     * @return A [HoeApiResult] containing a list of matching [Street] objects.
     */
    suspend fun searchStreets(query: String, settlementId: Int): HoeApiResult<List<Street>>

    /**
     * Fetches the house numbers for a specified street.
     *
     * @param streetId The ID of the street to fetch house numbers for.
     * @return A [HoeApiResult] containing a list of house numbers as strings.
     */
    suspend fun fetchHouseNumbers(streetId: Int): HoeApiResult<List<String>>

    /**
     * Fetches power outage information for a specified settlement, street, and house number.
     *
     * @param settlementId The ID of the settlement.
     * @param streetId The ID of the street.
     * @param houseNumber The house number to fetch power outage information for.
     * @return A [HoeApiResult] containing the [PowerOutageEvent] details.
     */
    suspend fun fetchPowerOutage(settlementId: Int, streetId: Int, houseNumber: String): HoeApiResult<PowerOutageEvent>
}
