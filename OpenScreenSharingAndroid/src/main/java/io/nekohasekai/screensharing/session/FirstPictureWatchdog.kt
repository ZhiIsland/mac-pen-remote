package io.nekohasekai.screensharing.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Waits for a displayed frame, including negotiation and hardware decoding. */
internal class FirstPictureWatchdog(private val scope: CoroutineScope) {
    private var job: Job? = null

    fun start(isBackground: () -> Boolean, isPending: () -> Boolean, timedOut: () -> Unit) {
        cancel()
        job = scope.launch {
            delay(TIMEOUT_MILLIS)
            while (isPending() && isBackground()) delay(500)
            if (isPending()) timedOut()
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    companion object {
        const val TIMEOUT_MILLIS = 12_000L
    }
}
