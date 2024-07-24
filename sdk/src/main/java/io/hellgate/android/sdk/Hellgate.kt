package io.hellgate.android.sdk

import androidx.annotation.MainThread
import arrow.core.getOrElse
import arrow.core.raise.either
import io.hellgate.android.sdk.Constants.HG_URL
import io.hellgate.android.sdk.client.hellgate.*
import io.hellgate.android.sdk.client.hellgate.NextAction
import io.hellgate.android.sdk.handler.*
import io.hellgate.android.sdk.model.InvalidSessionState
import io.hellgate.android.sdk.service.tokenService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object Constants {
    const val HG_URL = "https://api.hellgate.io"
}

interface Hellgate {
    @MainThread
    suspend fun fetchSessionStatus(): SessionState

    @MainThread
    suspend fun cardHandler(): Result<CardHandler>
}

@MainThread
fun initHellgate(
    sessionId: String,
    hgBaseUrl: String = HG_URL,
): Hellgate = internalHellgate(sessionId, hgBaseUrl)

internal fun internalHellgate(
    sessionId: String,
    hgBaseUrl: String,
    client: () -> HgClient = { hgClient(hgBaseUrl) },
) = object : Hellgate {
    private val mutex = Mutex()
    private var sessionState = SessionState.UNKNOWN

    override suspend fun fetchSessionStatus(): SessionState =
        mutex.withLock {
            either {
                val sessionInfo = client().use { it.fetchSession(sessionId) }.bind()
                when (sessionInfo.nextAction) {
                    NextAction.TOKENIZE_CARD -> SessionState.REQUIRE_TOKENIZATION
                    NextAction.WAIT -> SessionState.WAITING

                    null -> when (sessionInfo.status) {
                        "success" -> SessionState.COMPLETED
                        else -> SessionState.UNKNOWN
                    }
                }
            }.getOrElse { SessionState.UNKNOWN }
                .also { sessionState = it }
        }

    override suspend fun cardHandler(): Result<CardHandler> {
        fetchSessionStatus()
        return mutex.withLock {
            if (sessionState != SessionState.REQUIRE_TOKENIZATION) {
                Result.failure(InvalidSessionState.notTokenizeCard(sessionState.name))
            } else {
                Result.success(cardHandler(tokenService(hgBaseUrl), sessionId))
            }
        }
    }
}

enum class SessionState {
    REQUIRE_TOKENIZATION,
    WAITING,
    COMPLETED,
    UNKNOWN,
}
