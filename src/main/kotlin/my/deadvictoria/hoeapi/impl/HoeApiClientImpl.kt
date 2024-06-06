package my.deadvictoria.hoeapi.impl

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import my.deadvictoria.hoeapi.exception.HoeApiException
import my.deadvictoria.hoeapi.models.Pem
import my.deadvictoria.hoeapi.models.PowerCutEvent
import my.deadvictoria.hoeapi.models.Type
import my.deadvictoria.hoeapi.spi.HoeApiClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO:
// - Caching
// - Logging
internal class HoeApiClientImpl(
    private val client: HttpClient = HttpClient(CIO)
) : HoeApiClient {

    private companion object {
        val createdAtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val estimatedTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }

    override suspend fun fetchAllPems(): List<Pem> {
        val html = client.get("https://hoe.com.ua/shutdown/all").bodyAsText()
        return Jsoup.parse(html)
            .select("select.form-control.select-rem")
            .first()
            ?.children()
            ?.filter { elem -> elem.attr("value").isNotBlank() }
            ?.map { elem ->
                Pem(
                    id = elem.attr("value"),
                    name = elem.text()
                )
            } ?: throw HoeApiException("Failed to fetch PEMs")
    }

    override suspend fun fetchAllActualPowerCuts(pemId: String): List<PowerCutEvent> = coroutineScope {
        val dateRange = fetchDateRange()
        val unplanned = async { fetchPowerCuts(Type.UNPLANNED, pemId, dateRange) }
        val planned = async { fetchPowerCuts(Type.PLANNED, pemId, dateRange) }
        return@coroutineScope unplanned.await() + planned.await()
    }

    private suspend fun fetchPowerCuts(
        type: Type,
        pemId: String,
        dateRange: String
    ): List<PowerCutEvent> = coroutineScope {
        val response: HttpResponse = client.request("https://hoe.com.ua/shutdown/eventlist") {
            method = HttpMethod.Post
            setBody(FormDataContent(
                Parameters.build {
                    append("TypeId", type.typeId)
                    append("PageNumber", "1")
                    append("RemId", pemId)
                    append("DateRange", dateRange)
                    append("X-Requested-With", "XMLHttpRequest")
                }
            ))

        }
        val html = response.bodyAsText()
        val doc = Jsoup.parse(html)
        val tableElem = doc.select("table.table-post-list.table-shutdowns").first()
            ?: throw HoeApiException("Failed to parse table")
        val rows = tableElem.select("tbody > tr")
            ?: throw HoeApiException("Failed to parse rows")
        val groupedRows = mutableListOf<Pair<Element, Element>>()
        rows.forEachIndexed { index, element ->
            if (index % 2 == 0 && index + 1 < rows.size) {
                groupedRows.add(Pair(element, rows[index + 1]))
            }
        }
        val pemName = doc.select("h3.heading.events-heading")
            .first()
            ?.text()
            ?.substringAfter(" - ")
            ?: throw HoeApiException("Failed to parse PEM name")
        val pem = Pem(pemId, pemName)
        val deferredEvents = groupedRows.map { (dataRow, streetsRow) ->
            async { parseEvent(dataRow, streetsRow, pem, type) }
        }
        return@coroutineScope deferredEvents.awaitAll()
    }

    private suspend fun parseEvent(
        dataRow: Element,
        streetsRow: Element,
        pem: Pem,
        type: Type
    ): PowerCutEvent = coroutineScope {
        val settlement = async {
            dataRow.child(0).select("p.city").first()?.text()?.trim()
                ?: throw HoeApiException("Failed to parse settlement [dataRow=$dataRow]")
        }
        val typeOfWork = dataRow.child(1).text().trim()
        val createdAt = async {
            dataRow.child(2)
                .select("div.stime")
                .first()
                ?.text()
                ?.trim()
                ?.let { LocalDate.parse(it, createdAtFormatter) }
                ?: throw HoeApiException("Failed to parse createdAt [dataRow=$dataRow]")
        }
        val estimatedStartTime = async {
            dataRow.child(3)
                .select("div.stime")
                .first()
                ?.let { LocalDateTime.parse(it.text().trim(), estimatedTimeFormatter) }
                ?: throw HoeApiException("Failed to parse estimatedStartTime [dataRow=$dataRow]")
        }
        val estimatedEndTime = async {
            dataRow.child(4)
                .select("div.stime")
                .first()
                ?.let { LocalDateTime.parse(it.text().trim(), estimatedTimeFormatter) }
                ?: throw HoeApiException("Failed to parse estimatedEndTime [dataRow=$dataRow]")
        }

        val streetGroups = async {
            streetsRow.child(0).children().map(Element::text)
                .map(HoeStreetGroupParser::parse)
                .toList()
        }

        return@coroutineScope PowerCutEvent(
            settlement = settlement.await(),
            pem = pem,
            streetGroups = streetGroups.await(),
            type = type,
            typeOfWork = typeOfWork,
            createdAt = createdAt.await(),
            estimatedStartTime = estimatedStartTime.await(),
            estimatedEndTime = estimatedEndTime.await()
        )
    }

    private suspend fun fetchDateRange(): String {
        val html = client.get("https://hoe.com.ua/shutdown/all").bodyAsText()
        return Jsoup.parse(html)
            .select("input[name=DateRange]")
            .first()
            ?.attr("value") ?: throw HoeApiException("Failed to fetch date range")
    }
}
