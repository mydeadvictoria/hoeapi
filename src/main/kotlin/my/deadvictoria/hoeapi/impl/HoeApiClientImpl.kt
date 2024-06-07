package my.deadvictoria.hoeapi.impl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import my.deadvictoria.hoeapi.models.HoeApiResult
import my.deadvictoria.hoeapi.models.PowerOutageEvent
import my.deadvictoria.hoeapi.models.PowerOutageType
import my.deadvictoria.hoeapi.models.Settlement
import my.deadvictoria.hoeapi.models.Street
import my.deadvictoria.hoeapi.spi.HoeApiClient
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class HoeApiClientImpl(
    private val client: HttpClient
) : HoeApiClient {

    private companion object {
        val estimatedTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }

    override suspend fun searchSettlements(query: String): HoeApiResult<List<Settlement>> {
        val response: HttpResponse = client.get("https://hoe.com.ua/settlements") {
            parameter("term", query)
            parameter("_type", "query")
            parameter("q", query)

            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
                append("X-Requested-With", "XMLHttpRequest")
            }
        }

        if (!response.status.isSuccess()) {
            return HoeApiResult.Error.withContext(
                "Failed to search settlements",
                "httpStatus" to response.status.value,
                "query" to query
            )
        }

        return HoeApiResult.Ok(response.body())
    }

    override suspend fun searchStreets(query: String, settlementId: Int): HoeApiResult<List<Street>> {
        val response: HttpResponse = client.get("https://hoe.com.ua/streets/$settlementId") {
            parameter("term", query)
            parameter("_type", "query")
            parameter("q", query)

            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
                append("X-Requested-With", "XMLHttpRequest")
            }
        }

        if (!response.status.isSuccess()) {
            return HoeApiResult.Error.withContext(
                "Failed to search streets",
                "httpStatus" to response.status.value,
                "query" to query,
                "settlementId" to settlementId
            )
        }

        return HoeApiResult.Ok(response.body())
    }

    override suspend fun fetchHouseNumbers(streetId: Int): HoeApiResult<List<String>> {
        val response: HttpResponse = client.get("https://hoe.com.ua/houses/$streetId") {
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
                append("X-Requested-With", "XMLHttpRequest")
            }
        }

        if (!response.status.isSuccess()) {
            return HoeApiResult.Error.withContext(
                "Failed to fetch house numbers",
                "httpStatus" to response.status.value,
                "streetId" to streetId
            )
        }

        return HoeApiResult.Ok(response.body())
    }

    override suspend fun fetchPowerOutage(
        settlementId: Int,
        streetId: Int,
        houseNumber: String
    ): HoeApiResult<PowerOutageEvent> {
        val response: HttpResponse = client.post("https://hoe.com.ua/shutdown-events") {
            setBody(FormDataContent(
                Parameters.build {
                    append("streetId", streetId.toString())
                    append("house", houseNumber)
                }
            ))

            headers {
                append("X-Requested-With", "XMLHttpRequest")
            }
        }

        val bodyText = response.bodyAsText()
        val commonContext = mapOf(
            "httpStatus" to response.status.value,
            "settlementId" to settlementId,
            "streetId" to streetId,
            "houseNumber" to houseNumber,
            "bodyBlank" to bodyText.isBlank()
        )

        if (!response.status.isSuccess() || bodyText.isBlank() || isErrorInBody(bodyText)) {
            return HoeApiResult.Error("Failed to fetch power outage event", commonContext)
        }

        if (isOutageAbsent(bodyText)) {
            return HoeApiResult.Ok(PowerOutageEvent.NoOutage(settlementId, streetId, houseNumber))
        }

        val tds = Jsoup.parse(bodyText).select("td")

        if (tds.size != 5) {
            return HoeApiResult.Error.withContext<PowerOutageEvent>(
                "Failed to parse power outage event",
                "tds" to tds
            ).addContext(commonContext)
        }

        val typeOfWork = tds[0].text()
        val type = PowerOutageType.parse(tds[1].text())
        val schedule = tds[2].text().toIntOrNull() ?: 0
        val startTime = LocalDateTime.parse(tds[3].text(), estimatedTimeFormatter)
        val endTime = LocalDateTime.parse(tds[4].text(), estimatedTimeFormatter)

        return HoeApiResult.Ok(
            PowerOutageEvent.ActiveOutage(
                settlementId = settlementId,
                streetId = streetId,
                houseNumber = houseNumber,
                typeOfWork = typeOfWork,
                type = type,
                schedule = schedule,
                estimatedStartTime = startTime,
                estimatedEndTime = endTime
            )
        )
    }

    private fun isOutageAbsent(body: String): Boolean =
        body.contains("За вказаною адресою відсутнє зареєстроване відключення")

    private fun isErrorInBody(body: String): Boolean =
        body.contains("Помилка")
}
