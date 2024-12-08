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
import my.deadvictoria.hoeapi.models.ScheduleImage
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
        private val HOE_HOST: String = "https://hoe.com.ua"
        private val SCHEDULE_URL: String = "https://hoe.com.ua/page/pogodinni-vidkljuchennja"
        val estimatedTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }

    override suspend fun searchSettlements(query: String): HoeApiResult<List<Settlement>> = runCatching {
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
            return@runCatching HoeApiResult.Error.withContext(
                "Failed to search settlements",
                "httpStatus" to response.status.value,
                "query" to query
            )
        }

        return@runCatching HoeApiResult.Ok(response.body())
    }

    override suspend fun searchStreets(query: String, settlementId: Int): HoeApiResult<List<Street>> = runCatching {
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
            return@runCatching HoeApiResult.Error.withContext(
                "Failed to search streets",
                "httpStatus" to response.status.value,
                "query" to query,
                "settlementId" to settlementId
            )
        }

        return@runCatching HoeApiResult.Ok(response.body())
    }

    override suspend fun fetchHouseNumbers(streetId: Int): HoeApiResult<List<String>> = runCatching {
        val response: HttpResponse = client.get("https://hoe.com.ua/houses/$streetId") {
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
                append("X-Requested-With", "XMLHttpRequest")
            }
        }

        if (!response.status.isSuccess()) {
            return@runCatching HoeApiResult.Error.withContext(
                "Failed to fetch house numbers",
                "httpStatus" to response.status.value,
                "streetId" to streetId
            )
        }

        return@runCatching HoeApiResult.Ok(response.body())
    }

    override suspend fun fetchPowerOutage(
        settlementId: Int,
        streetId: Int,
        houseNumber: String
    ): HoeApiResult<PowerOutageEvent> = runCatching {
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
            return@runCatching HoeApiResult.Error("Failed to fetch power outage event", commonContext)
        }

        if (isOutageAbsent(bodyText)) {
            return@runCatching HoeApiResult.Ok(PowerOutageEvent.NoOutage(settlementId, streetId, houseNumber))
        }

        val tds = Jsoup.parse(bodyText).select("td")

        if (tds.size != 5) {
            return@runCatching HoeApiResult.Error.withContext<PowerOutageEvent>(
                "Failed to parse power outage event",
                "tds" to tds
            ).addContext(commonContext)
        }

        val typeOfWork = tds[0].text()
        val type = PowerOutageType.parse(tds[1].text())
        val schedule = tds[2].text().toIntOrNull() ?: 0
        val startTime = LocalDateTime.parse(tds[3].text(), estimatedTimeFormatter)
        val endTime = LocalDateTime.parse(tds[4].text(), estimatedTimeFormatter)

        return@runCatching HoeApiResult.Ok(
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

    override suspend fun fetchQueues(streetId: Int, houseNumber: String): HoeApiResult<List<Int>> = runCatching {
        val response: HttpResponse = client.post("https://hoe.com.ua/shutdown-queues") {
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
            "streetId" to streetId,
            "houseNumber" to houseNumber,
            "bodyBlank" to bodyText.isBlank()
        )

        if (!response.status.isSuccess() || bodyText.isBlank()) {
            return@runCatching HoeApiResult.Error("Failed to fetch queues", commonContext)
        }

        val queuesString = Jsoup.parse(bodyText).select("strong").text().trim()
        val queues = queuesString.split(",").map(String::trim).map { it.toInt() }

        return@runCatching HoeApiResult.Ok(queues)
    }

    override suspend fun fetchCurrentScheduleImage(): HoeApiResult<ScheduleImage> = runCatching {
        val body = client.get(SCHEDULE_URL).bodyAsText()
        val imgElem = Jsoup.parse(body).selectFirst("div.post > p > img") ?:
            return@runCatching HoeApiResult.Ok(ScheduleImage.NoImage)

        if (!imgElem.hasAttr("src")) {
            return@runCatching HoeApiResult.Error(
                error = "Unable to find current schedule image: no src attr"
            )
        }

        val src = imgElem.attribute("src").value
        val alt = imgElem.attribute("alt")?.value
        return@runCatching HoeApiResult.Ok(ScheduleImage.Image(
            url = HOE_HOST + src,
            alt = alt
        ))
    }

    private fun isOutageAbsent(body: String): Boolean =
        body.contains("За вказаною адресою відсутнє зареєстроване відключення")

    private fun isErrorInBody(body: String): Boolean =
        body.contains("Помилка")

    private suspend fun <T> runCatching(block: suspend () -> HoeApiResult<T>): HoeApiResult<T> {
        return try {
            block()
        } catch (ex: Exception) {
            HoeApiResult.Error(
                error = "Exception has been thrown",
                exception = ex
            )
        }
    }
}
