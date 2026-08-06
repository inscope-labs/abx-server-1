package com.inscopelabs.abx.server.core.dispatcher

import android.content.Context

object DispatcherRouterProvider {
    private var instance: DispatcherRouter? = null

    @Synchronized
    fun get(context: Context): DispatcherRouter {
        val current = instance
        if (current != null) return current
        val newInstance = DispatcherRouter(context.applicationContext)
        instance = newInstance
        return newInstance
    }

    @Synchronized
    fun setForTesting(router: DispatcherRouter?) {
        instance = router
    }
}
