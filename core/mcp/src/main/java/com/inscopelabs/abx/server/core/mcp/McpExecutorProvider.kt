package com.inscopelabs.abx.server.core.mcp

import android.content.Context
import com.inscopelabs.abx.server.core.policy.PolicyEngineImpl
import com.inscopelabs.abx.server.core.session.ReplayProtectionProvider

object McpExecutorProvider {
    private var instance: McpExecutor? = null

    @Synchronized
    fun get(context: Context): McpExecutor {
        val current = instance
        if (current != null) return current
        val newInstance = McpExecutor(
            PolicyEngineImpl(),
            FileSystemReaderImpl(context.applicationContext),
            ReplayProtectionProvider.get(context.applicationContext)
        )
        instance = newInstance
        return newInstance
    }

    @Synchronized
    fun setForTesting(executor: McpExecutor?) {
        instance = executor
    }
}
