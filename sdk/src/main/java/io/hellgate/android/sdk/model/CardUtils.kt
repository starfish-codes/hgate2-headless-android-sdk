package io.hellgate.android.sdk.model

internal object CardUtils {
    /**
     * Checks the input string to see whether or not it is a valid Luhn number.
     *
     * @param cardNumber a String that may or may not represent a valid Luhn number
     * @return `true` if and only if the input value is a valid Luhn number
     */
    @SuppressWarnings("MagicNumber")
    fun isValidLuhnNumber(cardNumber: String?): Boolean {
        if (cardNumber == null) return false

        var isOdd = true
        var sum = 0

        for (index in cardNumber.length - 1 downTo 0) {
            val c = cardNumber[index]
            if (!c.isDigit()) {
                return false
            }

            var digitInteger = Character.getNumericValue(c)
            isOdd = !isOdd

            if (isOdd) {
                digitInteger *= 2
            }

            if (digitInteger > 9) {
                digitInteger -= 9
            }

            sum += digitInteger
        }

        return sum % 10 == 0
    }
}
