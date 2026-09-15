package com.example.ui.keyboard

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modifier that triggers an initial click immediately, and then repeatedly triggers [onAction]
 * at [repeatIntervalMillis] after an initial hold delay [initialDelayMillis] until released.
 */
fun Modifier.repeatingClickable(
    initialDelayMillis: Long = 400L,
    repeatIntervalMillis: Long = 60L,
    enabled: Boolean = true,
    onAction: () -> Unit
): Modifier {
    if (!enabled) return this
    return this.pointerInput(enabled) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                onAction()

                val job = launch {
                    delay(initialDelayMillis)
                    while (true) {
                        onAction()
                        delay(repeatIntervalMillis)
                    }
                }
                waitForUpOrCancellation()
                job.cancel()
            }
        }
    }
}
