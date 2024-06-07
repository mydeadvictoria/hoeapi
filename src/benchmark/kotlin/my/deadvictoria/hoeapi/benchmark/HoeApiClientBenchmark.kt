package my.deadvictoria.hoeapi.benchmark

import kotlinx.benchmark.*
import kotlinx.coroutines.runBlocking
import my.deadvictoria.hoeapi.spi.HoeApiClient
import my.deadvictoria.hoeapi.spi.HoeApiClientFactory
import org.openjdk.jmh.annotations.Mode
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class HoeApiClientBenchmark {

    private lateinit var client: HoeApiClient
    private lateinit var searchSettlementsInputs: List<String>
    private lateinit var searchStreetsInputs: List<String>
    private lateinit var fetchHousesInputs: List<Int>

    @Setup
    fun setup() {
        client = HoeApiClientFactory.create()
        searchSettlementsInputs = listOf("Хмельницький", "ада", "шпи", "яс", "по", "кло", "дера", "анд", "май")
        searchStreetsInputs = listOf("Проск", "Кам", "Под", "проспект", "Собор", "Заріч", "Прибуз", "Свобо")
        fetchHousesInputs = listOf(0, 340712, 340653, 340651, 280915, 280916, 280612, 280617, 280542)
    }

    @Benchmark
    fun measureSearchSettlements(blackhole: Blackhole): Unit = runBlocking {
        val resutls = client.searchSettlements(searchSettlementsInputs.random())
        blackhole.consume(resutls)
    }

    @Benchmark
    fun measureSearchStreets(blackhole: Blackhole): Unit = runBlocking {
        val resutls = client.searchStreets(searchStreetsInputs.random(), 26499)
        blackhole.consume(resutls)
    }

    @Benchmark
    fun measureFetchHouses(blackhole: Blackhole): Unit = runBlocking {
        val resutls = client.fetchHouseNumbers(fetchHousesInputs.random())
        blackhole.consume(resutls)
    }

    @Benchmark
    fun measureAllTogether(blackhole: Blackhole): Unit = runBlocking {
        val settlements = client.searchSettlements("Хмельницький").getOrThrow()
        blackhole.consume(settlements)
        val streets = client.searchStreets("зарічанська", 26499).getOrThrow()
        blackhole.consume(streets)

        val streetId = 280542
        val settlementId = 26499
        val houseNumbers = client.fetchHouseNumbers(streetId).getOrThrow()
        val results = client.fetchPowerOutage(settlementId, streetId, houseNumbers.random())
        blackhole.consume(results)
    }

}
