package my.deadvictoria.hoeapi.impl

import my.deadvictoria.hoeapi.models.StreetGroup

internal object HoeStreetGroupParser {

    private val houseNumbersSplitRegex: Regex = """\s*,\s*""".toRegex()

    fun parse(input: String): StreetGroup {
        val input = input.trim()
        return if (input.contains(',')) {
            val firstCommaIdx = input.indexOfFirst { it == ',' }
            val splitIndex = input.substring(0, firstCommaIdx).lastIndexOf(' ')
            val streetName = input.substring(0, splitIndex)
            val houseNumbers = input.substring(splitIndex + 1).split(houseNumbersSplitRegex)
            StreetGroup(streetName, houseNumbers.toSet())
        } else {
            val houseNumber = input.split(" ").last()
            val streetName = input.removeSuffix(houseNumber).trim()
            StreetGroup(streetName, setOf(houseNumber))
        }

    }
}
