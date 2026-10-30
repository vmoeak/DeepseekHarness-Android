package io.github.jackwener.dsh.core.capability

import org.junit.Assert.*
import org.junit.Test

class ScreenRoutingTest {
    @Test fun explicitAgentRequestNeverFallsBackToMain() {
        for (reason in listOf("Shizuku is not ready", "No agent screen exists", "createVirtualDisplay failed")) {
            val failure = assertThrows(AgentScreenUnavailableException::class.java) {
                requireRequestedScreen("agent", ScreenTarget(0, false, reason))
            }
            assertTrue(failure.message!!.contains(reason))
            assertTrue(failure.message!!.contains("The user's screen was not used"))
        }
    }

    @Test fun liveAgentDisplayIsPreserved() {
        val target = ScreenTarget(7, true)
        assertSame(target, requireRequestedScreen("agent", target))
        assertSame(target, requireRequestedScreen(null, target))
    }

    @Test fun explicitMainAndNormalDefaultWithoutAgentRemainAvailable() {
        val main = ScreenTarget(0, false)
        assertSame(main, requireRequestedScreen("main", main))
        assertSame(main, requireRequestedScreen(null, main))
    }
}
