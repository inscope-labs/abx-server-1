package com.inscopelabs.abx.server.core.tunnel

import android.content.Context
import com.inscopelabs.abx.server.core.mcp.McpExecutorProvider
import com.inscopelabs.abx.server.core.policy.CapabilityStoreProvider
import com.inscopelabs.abx.server.core.session.SessionManagerProvider

object TunnelManagerProvider {
    private var instance: TunnelManager? = null

    @Synchronized
    fun get(context: Context): TunnelManager {
        val current = instance
        if (current != null) return current
        val appContext = context.applicationContext
        val dispatcher = McpDispatcher(
            McpExecutorProvider.get(appContext),
            CapabilityStoreProvider.get(appContext),
            SessionManagerProvider.get(appContext)
        )
        val newInstance = TunnelManagerImpl(
            appContext,
            SessionManagerProvider.get(appContext),
            dispatcher = dispatcher
        )
        instance = newInstance
        return newInstance
    }

    @Synchronized
    fun setForTesting(tunnelManager: TunnelManager?) {
        instance = tunnelManager
    }
}
