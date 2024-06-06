package my.deadvictoria.hoeapi.impl

import my.deadvictoria.hoeapi.models.StreetGroup
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class HoeStreetGroupParserTest {

    @Test
    fun `parse group with a single house number`() {
        val output = HoeStreetGroupParser.parse("пров. Мирний 3/4")!!
        expectThat(output.street).isEqualTo("пров. Мирний")
        expectThat(output.houseNumbers).containsExactly("3/4")
    }

    @Test
    fun `parse group with multiple consecutive whitespaces`() {
        val output = HoeStreetGroupParser.parse("пров. Мирний 3/4,  13,   5,2")!!
        expectThat(output.street).isEqualTo("пров. Мирний")
        expectThat(output.houseNumbers).containsExactly("3/4", "13", "5", "2")
    }

    @ParameterizedTest(name = "Test {index}: parse({0})")
    @MethodSource("streetsAndHouseNumbers")
    fun parse(input: String, expectedOutput: StreetGroup) {
        val output = HoeStreetGroupParser.parse(input)!!
        expectThat(output.street).isEqualTo(expectedOutput.street)
        expectThat(output.houseNumbers).containsExactly(expectedOutput.houseNumbers)
    }

    private companion object {
        @JvmStatic
        fun streetsAndHouseNumbers() = listOf(
            Arguments.of(
                "пров. Урожайний 1, 2, 3, 4/5",
                StreetGroup.of("пров. Урожайний", "1", "2", "3", "4/5")
            ),
            Arguments.of(
                "пров. Шкільний 1/2",
                StreetGroup.of("пров. Шкільний", "1/2")
            ),
            Arguments.of(
                "вул. 70-річчя Жовтня 1A, 7, 9B, 11, 13C, 19-F",
                StreetGroup.of("вул. 70-річчя Жовтня", "1A", "7", "9B", "11", "13C", "19-F")
            ),
            Arguments.of(
                "проїзд Підкови Івана 7-й 1, 3, 5, 7, 9, 10",
                StreetGroup.of("проїзд Підкови Івана 7-й", "1", "3", "5", "7", "9", "10")
            ),
            Arguments.of(
                "вул. 269км+710м Стрий-Тернопіль-Кропивницький-Знамянка 710",
                StreetGroup.of("вул. 269км+710м Стрий-Тернопіль-Кропивницький-Знамянка", "710")
            ),
            Arguments.of(
                "вул. землі с/р 411, 463, 543",
                StreetGroup.of("вул. землі с/р", "411", "463", "543")
            ),
            Arguments.of(
                "вул. Трембовецької М. 1, 3, 21A, 13/1",
                StreetGroup.of("вул. Трембовецької М.", "1", "3", "21A", "13/1")
            ),
            Arguments.of(
                "пр. Миру 60А",
                StreetGroup.of("пр. Миру", "60А")
            ),
            Arguments.of(
                "вул. Інститутська 1, 6/4Г",
                StreetGroup.of("вул. Інститутська", "1", "6/4Г")
            ),
            Arguments.of(
                "вул. 131-й км 8",
                StreetGroup.of("вул. 131-й км", "8")
            ),
            Arguments.of(
                "проїзд д 3-й І.Підкови 1, 2, 4, 7",
                StreetGroup.of("проїзд д 3-й І.Підкови", "1", "2", "4", "7")
            )
        )
    }
}
