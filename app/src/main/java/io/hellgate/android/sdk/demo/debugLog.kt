package io.hellgate.android.sdk.demo

import android.util.Log

inline fun <reified T> T.debugLog(string: String) = Log.d(T::class.java.simpleName, string)
