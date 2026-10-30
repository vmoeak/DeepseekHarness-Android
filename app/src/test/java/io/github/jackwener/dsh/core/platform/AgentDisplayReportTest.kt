package io.github.jackwener.dsh.core.platform

import org.junit.Assert.*
import org.junit.Test

class AgentDisplayReportTest {
    @Test fun adbUsesShellPackageRatherThanAndroidOrTheClientPackage() {
        assertEquals("com.android.shell", displayPackageForUid(2000))
        assertEquals("android", displayPackageForUid(0))
        assertEquals("android", displayPackageForUid(1000))
        assertThrows(IllegalStateException::class.java) { displayPackageForUid(10234) }
    }

    @Test fun remoteFailureRetainsIdentityStageAndAllFlagErrors() {
        val original = AgentDisplayReport(
            stage = "create_virtual_display", serviceUid = 2000, servicePid = 55, callerUid = 10234,
            packageName = "com.android.shell", error = "Every virtual display flag combination failed",
            attempts = listOf(DisplayAttempt(0x1049, "SecurityException: permission denied\nflags rejected"), DisplayAttempt(0x409, "DisplayManager returned null")),
        )
        val decoded = AgentDisplayReport.decode(AgentDisplayReport.encode(original))
        assertEquals(original, decoded)
        assertFalse(decoded.created)
        assertTrue(decoded.describe().contains("serviceUid=2000"))
        assertTrue(decoded.describe().contains("flags=0x1049: SecurityException"))
        assertTrue(decoded.describe().contains("flags=0x409: DisplayManager returned null"))
    }

    @Test fun defaultDisplayAndIncompleteRepliesNeverCountAsBackgroundSuccess() {
        val report = AgentDisplayReport(displayId = 7, stage = "created", serviceUid = 2000, servicePid = 55, callerUid = 10234)
        assertTrue(report.created)
        assertFalse(report.copy(displayId = 0).created)
        assertFalse(report.copy(displayId = -1).created)
        assertFalse(report.copy(stage = "context").created)
        assertFalse(report.copy(error = "failure").created)
        assertThrows(Exception::class.java) { AgentDisplayReport.decode("-1") }
        assertThrows(Exception::class.java) { AgentDisplayReport.decode("{}") }
    }

    @Test fun wrappedSystemErrorsExposeTheirActualCause() {
        val wrapped = java.lang.reflect.InvocationTargetException(SecurityException("packageName must match the calling uid"))
        val detail = displayErrorDetails(wrapped)
        assertTrue(detail.contains("InvocationTargetException"))
        assertTrue(detail.contains("SecurityException: packageName must match the calling uid"))
        assertTrue(detail.contains("AgentDisplayReportTest"))
    }
}
