package io.hellgate.android.sdk.model

internal sealed class CardNumber {
    /**
     * A representation of a partial or full card number that hasn't been validated.
     */
    data class Unvalidated(
        private val denormalized: String,
    ) : CardNumber() {
        val normalized = denormalized.filterNot { REJECT_CHARS.contains(it) }

        val length = normalized.length

        val isMaxLength = length == MAX_PAN_LENGTH

        val bin: Bin? = Bin.create(normalized)

        private val isValidLuhn = CardUtils.isValidLuhnNumber(normalized)

        fun validate(panLength: Int): Validated? =
            if (panLength >= MIN_PAN_LENGTH &&
                normalized.length == panLength &&
                isValidLuhn
            ) {
                Validated(
                    value = normalized,
                )
            } else {
                null
            }

        /**
         * Format a number based on its expected length
         *
         * e.g. `"4242424242424242"` with pan length `16` will return `"4242 4242 4242 4242"`;
         * `"424242"` with pan length `16` will return `"4242 42"`;
         * `"4242424242424242"` with pan length `14` will return `"4242 424242 4242"`
         */
        fun getFormatted(panLength: Int = DEFAULT_PAN_LENGTH) = formatNumber(panLength)

        private fun formatNumber(panLength: Int): String {
            val spacePositions = getSpacePositions(panLength)
            val spacelessCardNumber = normalized.take(panLength)
            val groups = arrayOfNulls<String?>(spacePositions.size + 1)

            val length = spacelessCardNumber.length
            var lastUsedIndex = 0

            spacePositions
                .toList().sorted().forEachIndexed { idx, spacePosition ->
                    val adjustedSpacePosition = spacePosition - idx
                    if (length > adjustedSpacePosition) {
                        groups[idx] = spacelessCardNumber.substring(
                            lastUsedIndex,
                            adjustedSpacePosition,
                        )
                        lastUsedIndex = adjustedSpacePosition
                    }
                }

            // populate any remaining digits in the first index with a null value
            groups
                .indexOfFirst { it == null }
                .takeIf {
                    it != -1
                }?.let {
                    groups[it] = spacelessCardNumber.substring(lastUsedIndex)
                }

            return groups
                .takeWhile { it != null }
                .joinToString(" ")
        }

        fun isPartialEntry(panLength: Int) = (normalized.length != panLength) && normalized.isNotBlank()

        fun isPossibleCardBrand(): Boolean =
            normalized.isNotBlank() &&
                CardBrand.getCardBrands(normalized).first() != CardBrand.Unknown

        private companion object {
            // characters to remove from a denormalized number to make it normalized
            private val REJECT_CHARS = setOf('-', ' ')
        }
    }

    data class Validated(
        val value: String,
    ) : CardNumber()

    companion object {
        fun getSpacePositions(panLength: Int) =
            SPACE_POSITIONS[panLength]
                ?: DEFAULT_SPACE_POSITIONS

        const val MIN_PAN_LENGTH = 14

        const val MAX_PAN_LENGTH = 19
        const val DEFAULT_PAN_LENGTH = 16
        private val DEFAULT_SPACE_POSITIONS = setOf(4, 9, 14)

        private val SPACE_POSITIONS = mapOf(
            14 to setOf(4, 11),
            15 to setOf(4, 11),
            16 to setOf(4, 9, 14),
            19 to setOf(4, 9, 14, 19),
        )
    }
}
