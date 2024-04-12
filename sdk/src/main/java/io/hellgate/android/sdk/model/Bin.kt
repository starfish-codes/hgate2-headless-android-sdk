package io.hellgate.android.sdk.model

internal data class Bin(
    val value: String,
) {
    override fun toString() = value

    companion object {
        fun create(cardNumber: String): Bin? =
            cardNumber
                .take(BIN_LENGTH)
                .takeIf {
                    it.length == BIN_LENGTH
                }?.let {
                    Bin(it)
                }

        private const val BIN_LENGTH = 6
    }
}
