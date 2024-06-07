package my.deadvictoria.hoeapi.impl

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import my.deadvictoria.hoeapi.models.Settlement
import my.deadvictoria.hoeapi.models.Street
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.size

class HoeApiClientImplTest {

    @Test
    fun `searchSettlements()`(): Unit = runBlocking {
        val id = 26499
        val name = "м. Хмельницький (Хмельницька громада)"
        val client = mockedClient { _ ->
            respond(
                content = ByteReadChannel("""
                    [{"id": $id, "text": "$name"}]
                    """.trimIndent()
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val results = client.searchSettlements("Хмель").getOrThrow()
        expectThat(results).size.isEqualTo(1)
        expectThat(results[0]).get(Settlement::id).isEqualTo(id)
        expectThat(results[0]).get(Settlement::name).isEqualTo(name)
    }

    @Test
    fun `searchStreets()`(): Unit = runBlocking {
        val id = 281050
        val name = "вул. Січових стрільців"
        val client = mockedClient { _ ->
            respond(
                content = ByteReadChannel("""
                    [{"id": $id, "text": "$name"}]
                    """.trimIndent()
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val results = client.searchStreets("січ", 26499).getOrThrow()
        expectThat(results).size.isEqualTo(1)
        expectThat(results[0]).get(Street::id).isEqualTo(id)
        expectThat(results[0]).get(Street::name).isEqualTo(name)
    }

    @Test
    fun `fetchHouses()`(): Unit = runBlocking {
        val houseNumbers = listOf("1", "2", "3/1", "4B", "7")
        val client = mockedClient { _ ->
            respond(
                content = ByteReadChannel("[${houseNumbers.joinToString(", ") { "\"$it\"" }}]"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val results = client.fetchHouseNumbers(33831).getOrThrow()
        expectThat(results).containsExactly(houseNumbers)
    }

    private fun mockedClient(
        handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): HoeApiClientImpl {
        return HoeApiClientImpl(HttpClient(MockEngine { handler(it) }) {
            install(ContentNegotiation) {
                json()
            }
        })
    }
}
