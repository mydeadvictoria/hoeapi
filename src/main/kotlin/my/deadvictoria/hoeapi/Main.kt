package my.deadvictoria.hoeapi

import my.deadvictoria.hoeapi.models.PowerCutEvent
import my.deadvictoria.hoeapi.spi.HoeApiClient
import my.deadvictoria.hoeapi.spi.HoeApiClientFactory
import kotlin.system.measureTimeMillis

suspend fun main() {
    val client: HoeApiClient = HoeApiClientFactory.create()
    val pems = client.fetchAllPems()

    println("PEMs:")
    pems.forEach { pem ->
        println("- ${pem.name} (${pem.id})")
    }
    println()

    var totalTimeMillis = 0.0
    val runs = 1
    var results = listOf<PowerCutEvent>()

    repeat(runs) {
        val duration = measureTimeMillis {
            results = client.fetchAllActualPowerCuts("21")
        }
        totalTimeMillis += duration
        println("Run ${it + 1}: Execution time: ${duration.toDouble() / 1000}s")
    }

    println()
    results.forEach { powerCut ->
        println("Settlement: ${powerCut.settlement}")
        println("PEM: ${powerCut.pem.name}")
        println("Type: ${powerCut.type}")
        println("Type of work: ${powerCut.typeOfWork}")
        println("Created at: ${powerCut.createdAt}")
        println("Starts approximately: ${powerCut.estimatedStartTime}")
        println("Ends approximately: ${powerCut.estimatedEndTime}")
        println("Streets:")
        powerCut.streetGroups.forEach { group ->
            println("    ${group.street} ${group.houseNumbers.joinToString(", ")}")
        }
        println()
    }

    val averageTimeMillis = totalTimeMillis / runs
    println("Average execution time over $runs runs: ${averageTimeMillis / 1000}s")
    println("Fetched ${results.size} power cut events in total")}
