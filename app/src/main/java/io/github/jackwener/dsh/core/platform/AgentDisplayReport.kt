package io.github.jackwener.dsh.core.platform

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DisplayAttempt(val flags: Int, val error: String)

/** Returned across the shell-service boundary, including failures that otherwise only reach logcat. */
@Serializable
data class AgentDisplayReport(
    val displayId: Int = -1,
    val stage: String,
    val serviceUid: Int,
    val servicePid: Int,
    val callerUid: Int,
    val packageName: String = "",
    val error: String? = null,
    val attempts: List<DisplayAttempt> = emptyList(),
) {
    // A background display must never resolve to the user's default display (0).
    val created get() = displayId > 0 && stage == "created" && error == null

    fun describe(): String = buildString {
        append("stage=$stage, serviceUid=$serviceUid, servicePid=$servicePid, callerUid=$callerUid, package=$packageName, displayId=$displayId")
        error?.let { append("\n$it") }
        attempts.forEach { append("\nflags=0x${it.flags.toString(16)}: ${it.error}") }
    }

    companion object {
        private val wire = Json { ignoreUnknownKeys = true }
        fun decode(raw: String): AgentDisplayReport = wire.decodeFromString(raw)
        fun encode(report: AgentDisplayReport): String = wire.encodeToString(serializer(), report)
    }
}

/** The package passed to DisplayManager must belong to the *service* UID, not its app caller. */
internal fun displayPackageForUid(uid: Int): String = when (uid) {
    0, 1000 -> "android"
    2000 -> "com.android.shell"
    else -> error("Unsupported privileged service UID: $uid")
}

/** Reflection often wraps the useful SecurityException in InvocationTargetException. */
internal fun displayErrorDetails(error: Throwable): String =
    generateSequence(error) { it.cause }.take(8)
        .joinToString(" <- ") { "${it.javaClass.simpleName}: ${it.message}" } +
        "\n" + error.stackTraceToString().take(4_000)
