package com.inscopelabs.abx.server.core.dispatcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.inscopelabs.abx.server.contractdispatcher.DispatcherContractConstants
import com.inscopelabs.abx.server.contractdispatcher.DispatcherRequest
import com.inscopelabs.abx.server.contractdispatcher.DispatcherResponse
import com.inscopelabs.abx.server.contractdispatcher.IDispatcherExecutor
import com.inscopelabs.abx.server.contractdispatcher.ProtocolCompatibility
import com.inscopelabs.abx.server.contractdispatcher.ProtocolVersionCheck
import com.inscopelabs.abx.server.core.audit.AuditLog
import com.inscopelabs.abx.server.core.audit.ReasonCode
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TIMEOUT_MS = 10_000L

class DispatcherRouter(private val context: Context) {

    suspend fun route(request: DispatcherRequest): DispatcherResponse {
        val versionCheck = ProtocolVersionCheck.check(
            request.protocolVersion,
            DispatcherContractConstants.PROTOCOL_VERSION
        )
        if (versionCheck is ProtocolCompatibility.Incompatible) {
            return DispatcherResponse(
                success = false,
                resultData = null,
                errorCode = DispatcherContractConstants.ERROR_CODE_PROTOCOL_VERSION_MISMATCH,
                errorMessage = versionCheck.reason,
                protocolVersion = DispatcherContractConstants.PROTOCOL_VERSION
            )
        }

        var serviceConnection: ServiceConnection? = null
        var isBound = false

        try {
            val response = withTimeout(TIMEOUT_MS) {
                val binder = suspendCancellableCoroutine { continuation ->
                    val connection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                            if (continuation.isActive) {
                                if (service != null) {
                                    continuation.resume(service)
                                } else {
                                    continuation.resumeWithException(
                                        IllegalStateException("Service binder is null")
                                    )
                                }
                            }
                        }

                        override fun onServiceDisconnected(name: ComponentName?) {
                            // Service disconnected
                        }
                    }

                    serviceConnection = connection

                    val intent = Intent(DispatcherContractConstants.SERVICE_ACTION).apply {
                        setPackage(DispatcherContractConstants.TARGET_PACKAGE_NAME)
                        setClassName(
                            DispatcherContractConstants.TARGET_PACKAGE_NAME,
                            DispatcherContractConstants.TARGET_SERVICE_CLASS_NAME
                        )
                    }

                    val bound = try {
                        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    } catch (e: Exception) {
                        false
                    }

                    if (bound) {
                        isBound = true
                        continuation.invokeOnCancellation {
                            try {
                                context.unbindService(connection)
                            } catch (_: Exception) {}
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                IllegalStateException("Failed to bind service (not found or permission denied)")
                            )
                        }
                    }
                }
                val executor = IDispatcherExecutor.Stub.asInterface(binder)
                executor.execute(request)
            }

            return response
        } catch (e: TimeoutCancellationException) {
            AuditLog.recordRejection(
                ReasonCode.DISPATCHER_UNREACHABLE,
                request.sessionId,
                "bind timeout"
            )
            return DispatcherResponse(
                success = false,
                resultData = null,
                errorCode = null,
                errorMessage = "Dispatcher unreachable — request rejected",
                protocolVersion = DispatcherContractConstants.PROTOCOL_VERSION
            )
        } catch (e: Throwable) {
            val details = e.message ?: "Dispatcher connection failed"
            AuditLog.recordRejection(
                ReasonCode.DISPATCHER_UNREACHABLE,
                request.sessionId,
                details
            )
            return DispatcherResponse(
                success = false,
                resultData = null,
                errorCode = null,
                errorMessage = "Dispatcher unreachable — request rejected",
                protocolVersion = DispatcherContractConstants.PROTOCOL_VERSION
            )
        } finally {
            if (isBound && serviceConnection != null) {
                try {
                    context.unbindService(serviceConnection!!)
                } catch (_: Exception) {}
            }
        }
    }
}
