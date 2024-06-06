package my.deadvictoria.hoeapi.models

import java.time.LocalDate
import java.time.LocalDateTime

 /**
 * Represents a single power cut event in a specific settlement, street, and houses.
 *
 * This data class encapsulates the details of a planned or unplanned power outage,
 * including the affected settlement, street, and the range of house numbers.
 **/
data class PowerCutEvent(
    /**
     * Affected town, city or village name.
     * Example: `м. Хмельницький (Хмельницька громада)`
     */
    val settlement: String,

    /**
     * Affected PEM - Район Eлектричних Мереж.
     */
    val pem: Pem,

    /**
     * List of the street groups.
     */
    val streetGroups: List<StreetGroup>,

    /**
     * Type of the power cut.
     */
    val type: Type,

    /**
     * Type of the work being done.
     * Example: `Графік погодинних відключень`
     */
    val typeOfWork: String,

    /**
     * The exact date when this event was added to the system.
     */
    val createdAt: LocalDate,

    /**
     * Estimated time when the power cut starts.
     */
    val estimatedStartTime: LocalDateTime,

    /**
     * Estimated time when the power cut ends.
     */
    val estimatedEndTime: LocalDateTime
)

/**
 * A street-to-house-numbers mapping,
 * basically street + all affected house numbers.
 */
data class StreetGroup(
    /**
     * Name of the street.
     * Example: `вул. Тиха`
     */
    val street: String,

    /**
     * List of the affected house numbers.
     * Example: `25, 27, 62, 64, 66, 68, 70, 72, 74, 76, 29/4`
     */
    val houseNumbers: Set<String>
) {
    companion object {
        fun of(street: String, vararg houseNumbers: String): StreetGroup =
            StreetGroup(street, houseNumbers.toSet())
    }
}

/**
 * Represents an affected PEM.
 */
data class Pem(
    /**
     * PEM ID.
     * Example: `17`
     */
    val id: String,

    /**
     * PEM name.
     * Example: `Хмельницький РЕМ`
     */
    val name: String
)

/**
 * The type of power outage.
 */
enum class Type(
    /**
     * The ID of the type.
     */
    val typeId: String,

    /**
     * The name of the type.
     */
    val typeName: String
) {
    /**
     * Maps to `Аварійні`.
     */
    UNPLANNED("1", "Аварійні"),

    /**
     * Maps to `Планові`.
     */
    PLANNED("2", "Планові");
}
