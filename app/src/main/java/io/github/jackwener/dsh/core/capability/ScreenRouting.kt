package io.github.jackwener.dsh.core.capability

class AgentScreenUnavailableException(reason: String) : Exception(
    "$reason\nThe user's screen was not used. Use shizuku_diagnose for details. Report this failure; do not switch to screen=main without the user's explicit agreement."
)

/** Applies equally to app launches, reads and gestures: an explicit background request fails closed. */
internal fun requireRequestedScreen(requested: String?, target: ScreenTarget): ScreenTarget {
    if (requested == "agent" && !target.agent) {
        throw AgentScreenUnavailableException(target.note ?: "No agent screen is available for this conversation.")
    }
    return target
}
