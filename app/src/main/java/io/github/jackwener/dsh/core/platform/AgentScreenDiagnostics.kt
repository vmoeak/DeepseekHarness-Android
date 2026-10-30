package io.github.jackwener.dsh.core.platform

import android.content.Context
import android.util.Log
import java.io.File
import java.time.Instant

/** App-readable diagnostics; shell UID logcat is not part of Node's engine.log. */
object AgentScreenDiagnostics {
    private const val LIMIT = 64_000
    private fun file(context: Context) = File(context.filesDir, "logs/agent-screen.log")

    @Synchronized
    fun record(context: Context, message: String) {
        Log.i("AgentScreen", message)
        runCatching {
            val f = file(context)
            f.parentFile?.mkdirs()
            val previous = if (f.exists()) f.readText().takeLast(LIMIT / 2) else ""
            f.writeText((previous + "\n${Instant.now()}\n$message\n").takeLast(LIMIT))
        }.onFailure { Log.w("AgentScreen", "could not persist display diagnostics", it) }
    }

    @Synchronized
    fun tail(context: Context): String = runCatching {
        file(context).takeIf { it.exists() }?.readText()?.takeLast(24_000)
    }.getOrNull() ?: "No agent-screen creation attempt recorded yet."
}
