package com.inscopelabs.abx.server.core.policy

import android.content.Context
import com.inscopelabs.abx.server.core.session.SessionManagerProvider

object CapabilityStoreProvider {
    private var instance: CapabilityStore? = null

    @Synchronized
    fun get(context: Context): CapabilityStore {
        val current = instance
        if (current != null) return current
        val newInstance = CapabilityStoreImpl(
            SessionManagerProvider.get(context.applicationContext)
        )
        instance = newInstance
        return newInstance
    }

    @Synchronized
    fun setForTesting(store: CapabilityStore?) {
        instance = store
    }
}
