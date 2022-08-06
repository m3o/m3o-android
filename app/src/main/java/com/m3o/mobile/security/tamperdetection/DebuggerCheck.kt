package com.m3o.mobile.security.tamperdetection

import android.os.Debug

internal fun guardDebugger(error: (() -> Unit) = {}, function: (() -> Unit)) {
    val isDebuggerAttached = Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    if (!isDebuggerAttached) {
        function.invoke()
    } else {
        error.invoke()
    }
}
