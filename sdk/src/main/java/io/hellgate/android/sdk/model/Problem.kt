package io.hellgate.android.sdk.model

internal interface Problem {
    val message: String
    val throwable: Throwable?
}

data class InvalidSessionState(
    override val message: String = "Session is not in correct state",
    override val throwable: Throwable? = null,
) : Throwable(message, throwable),
    Problem {
    companion object {
        fun notTokenizeCard(actualState: String) =
            InvalidSessionState("Session is not in correct state to tokenize card, actual state: $actualState")

        fun genericError() = InvalidSessionState("Session is not in correct state to tokenize card")
    }
}
