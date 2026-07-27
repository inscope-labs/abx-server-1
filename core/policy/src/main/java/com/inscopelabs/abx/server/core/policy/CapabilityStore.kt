package com.inscopelabs.abx.server.core.policy

import com.inscopelabs.abx.server.core.session.SessionManager
import com.inscopelabs.abx.server.core.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface CapabilityStore {
    fun getActive(): Capability?
    fun setActive(capability: Capability)
    fun clear()
}

class CapabilityStoreImpl(
    private val sessionManager: SessionManager,
    parentScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : CapabilityStore {

    private val supervisorJob = SupervisorJob(parentScope.coroutineContext[Job])
    private val scope = CoroutineScope(parentScope.coroutineContext + supervisorJob)

    @Volatile
    private var active: Capability? = null

    init {
        scope.launch {
            sessionManager.stateFlow.collect { state ->
                if (state !is SessionState.ACTIVE) {
                    active = null
                }
            }
        }
    }

    override fun getActive(): Capability? = active

    override fun setActive(capability: Capability) {
        active = capability
    }

    override fun clear() {
        active = null
    }
}
