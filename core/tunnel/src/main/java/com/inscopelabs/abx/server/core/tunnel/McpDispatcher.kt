package com.inscopelabs.abx.server.core.tunnel

import com.inscopelabs.abx.server.core.mcp.McpExecutor
import com.inscopelabs.abx.server.core.policy.CapabilityStore
import com.inscopelabs.abx.server.core.session.SessionManager
import org.json.JSONArray
import org.json.JSONObject

class McpDispatcher(
    private val mcpExecutor: McpExecutor,
    private val capabilityStore: CapabilityStore,
    private val sessionManager: SessionManager
) {
    fun dispatch(message: Message): Message {
        val capability = capabilityStore.getActive()
            ?: return Message(notAuthorizedResponse())

        val currentState = sessionManager.getState()
        val responseJson = mcpExecutor.execute(message.content, capability, currentState)
        return Message(responseJson)
    }

    private fun notAuthorizedResponse(): String {
        val errObj = JSONObject()
        errObj.put("jsonrpc", "2.0")
        val errorDetail = JSONObject()
        errorDetail.put("code", -32000)
        errorDetail.put("message", "No active capability for this session")
        errObj.put("error", errorDetail)
        val contentArr = JSONArray()
        val textObj = JSONObject()
        textObj.put("type", "text")
        textObj.put("text", "Error: No active capability for this session")
        contentArr.put(textObj)
        errObj.put("content", contentArr)
        errObj.put("isError", true)
        return errObj.toString()
    }
}
