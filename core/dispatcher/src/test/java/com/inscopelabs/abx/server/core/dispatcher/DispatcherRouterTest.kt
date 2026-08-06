package com.inscopelabs.abx.server.core.dispatcher

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import androidx.test.core.app.ApplicationProvider
import com.inscopelabs.abx.server.contractdispatcher.DispatcherContractConstants
import com.inscopelabs.abx.server.contractdispatcher.DispatcherRequest
import com.inscopelabs.abx.server.core.audit.AuditLog
import com.inscopelabs.abx.server.core.audit.ReasonCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DispatcherRouterTest {

    private lateinit var context: Context
    private lateinit var router: DispatcherRouter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AuditLog.clear()
        router = DispatcherRouter(context)
    }

    @Test
    fun route_protocolVersionMismatch_returnsFailClosedWithoutBind() = runTest {
        val request = DispatcherRequest(
            prompt = "Hello",
            originComponent = "test",
            arguments = emptyMap(),
            sessionId = "sess-1",
            protocolVersion = 999
        )

        val response = router.route(request)

        assertFalse(response.success)
        assertEquals(
            DispatcherContractConstants.ERROR_CODE_PROTOCOL_VERSION_MISMATCH,
            response.errorCode
        )
        assertNull(response.resultData)
        assertTrue(response.errorMessage?.contains("does not match") == true)
    }

    @Test
    fun route_serviceNotFound_returnsFailClosedAndLogsAudit() = runTest {
        val request = DispatcherRequest(
            prompt = "Hello",
            originComponent = "test",
            arguments = emptyMap(),
            sessionId = "sess-2",
            protocolVersion = DispatcherContractConstants.PROTOCOL_VERSION
        )

        val response = router.route(request)

        assertFalse(response.success)
        assertNull(response.errorCode)
        assertEquals("Dispatcher unreachable — request rejected", response.errorMessage)

        val auditEntries = AuditLog.getEntries()
        assertTrue(auditEntries.any { 
            it.optString("reasonCode") == ReasonCode.DISPATCHER_UNREACHABLE.name &&
            it.optString("sessionId") == "sess-2"
        })
    }

    @Test
    fun route_bindTimesOut_returnsFailClosedAndLogsAudit() = runTest {
        val timingOutContext = object : ContextWrapper(context) {
            override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
                // Simulate successful bind initiation without calling onServiceConnected
                return true
            }
        }
        val timeoutRouter = DispatcherRouter(timingOutContext)
        val request = DispatcherRequest(
            prompt = "Hello",
            originComponent = "test",
            arguments = emptyMap(),
            sessionId = "sess-3",
            protocolVersion = DispatcherContractConstants.PROTOCOL_VERSION
        )

        val response = timeoutRouter.route(request)

        assertFalse(response.success)
        assertNull(response.errorCode)
        assertEquals("Dispatcher unreachable — request rejected", response.errorMessage)

        val auditEntries = AuditLog.getEntries()
        assertTrue(auditEntries.any {
            it.optString("reasonCode") == ReasonCode.DISPATCHER_UNREACHABLE.name &&
            it.optString("sessionId") == "sess-3" &&
            it.optString("details") == "bind timeout"
        })
    }
}
