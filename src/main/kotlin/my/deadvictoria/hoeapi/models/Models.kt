package my.deadvictoria.hoeapi.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * A sealed class representing the result of an API call.
 *
 * @param T The type of the result.
 */
sealed class HoeApiResult<T> {

    /**
     * Represents a successful result.
     *
     * @param T The type of the result.
     * @property value The value of the successful result.
     */
    data class Ok<T>(val value: T) : HoeApiResult<T>()

    /**
     * Represents an error result.
     *
     * @param T The type of the result.
     * @property error The error message.
     * @property context Additional context about the error.
     */
    data class Error<T>(
        val error: String,
        val context: Map<String, Any> = emptyMap()
    ) : HoeApiResult<T>() {

        companion object {
            /**
             * Creates an [Error] instance with additional context.
             *
             * @param T The type of the result.
             * @param error The error message.
             * @param context The additional context as key-value pairs.
             * @return An [Error] instance with the provided context.
             */
            fun <T> withContext(error: String, vararg context: Pair<String, Any>): Error<T> =
                Error(error, context.toMap())
        }

        /**
         * Adds additional context to the error.
         *
         * @param context The additional context to add.
         * @return A new [Error] instance with the combined context.
         */
        fun <T> addContext(context: Map<String, Any>): Error<T> =
            Error(this.error, this.context + context)

        /**
         * Returns a string representation of the error, including its context if present.
         *
         * @return A string representation of the error.
         */
        override fun toString(): String = when (context.isEmpty()) {
            true -> "Error: ${this.error}"
            false -> "Error: ${this.error} [${context.entries.joinToString(", ")}]"
        }
    }

    /**
     * Returns the value if the result is [Ok], otherwise throws an [IllegalStateException].
     *
     * @return The value of the successful result.
     * @throws IllegalStateException If the result is an [Error].
     */
    @Throws(IllegalStateException::class)
    fun getOrThrow(): T = when (this) {
        is Ok -> this.value
        is Error -> throw IllegalStateException("The value is absent")
    }
}

/**
 * Enum representing different types of power outages.
 *
 * @property nameUkr The name of the power outage type in Ukrainian.
 */
enum class PowerOutageType(val nameUkr: String) {
    PLANNED("Планове"),
    EMERGENCY("Аварійне"),
    UNKNOWN("Невідомо");

    companion object {
        /**
         * Parses the given Ukrainian name and returns the corresponding [PowerOutageType].
         *
         * @param nameUkr The name of the power outage type in Ukrainian.
         * @return The corresponding [PowerOutageType], or [UNKNOWN] if no match is found.
         */
        fun parse(nameUkr: String): PowerOutageType {
            return entries.find { it.nameUkr == nameUkr } ?: UNKNOWN
        }
    }
}

/**
 * A sealed class representing a power outage event.
 *
 * @property settlementId The ID of the settlement.
 * @property streetId The ID of the street.
 * @property houseNumber The house number.
 */
sealed class PowerOutageEvent(
    open val settlementId: Int,
    open val streetId: Int,
    open val houseNumber: String
) {
    /**
     * Represents a state where there is no power outage.
     *
     * @property settlementId The ID of the settlement.
     * @property streetId The ID of the street.
     * @property houseNumber The house number.
     */
    data class NoOutage(
        override val settlementId: Int,
        override val streetId: Int,
        override val houseNumber: String
    ) : PowerOutageEvent(settlementId, streetId, houseNumber)

    /**
     * Represents a state where there is an active power outage.
     *
     * @property settlementId The ID of the settlement.
     * @property streetId The ID of the street.
     * @property houseNumber The house number.
     * @property typeOfWork The type of work being performed during the outage.
     * @property type The type of power outage.
     * @property schedule The schedule identifier for the outage.
     * @property estimatedStartTime The estimated start time of the outage.
     * @property estimatedEndTime The estimated end time of the outage.
     */
    data class ActiveOutage(
        override val settlementId: Int,
        override val streetId: Int,
        override val houseNumber: String,
        val typeOfWork: String,
        val type: PowerOutageType,
        val schedule: Int,
        val estimatedStartTime: LocalDateTime,
        val estimatedEndTime: LocalDateTime
    ) : PowerOutageEvent(settlementId, streetId, houseNumber)
}

/**
 * Data class representing a settlement.
 *
 * @property id The ID of the settlement.
 * @property name The name of the settlement.
 */
@Serializable
data class Settlement(
    @SerialName("id")
    val id: Int,

    @SerialName("text")
    val name: String
)

/**
 * Data class representing a street.
 *
 * @property id The ID of the street.
 * @property name The name of the street.
 */
@Serializable
data class Street(
    @SerialName("id")
    val id: Int,

    @SerialName("text")
    val name: String
)
