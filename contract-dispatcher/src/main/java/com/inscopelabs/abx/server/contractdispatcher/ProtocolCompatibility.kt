package com.inscopelabs.abx.server.contractdispatcher

sealed class ProtocolCompatibility {
    object Compatible : ProtocolCompatibility()
    data class Incompatible(val reason: String) : ProtocolCompatibility()
}

object ProtocolVersionCheck {
    fun check(clientVersion: Int, hostVersion: Int): ProtocolCompatibility {
        return if (clientVersion == hostVersion) {
            ProtocolCompatibility.Compatible
        } else {
            ProtocolCompatibility.Incompatible(
                "Client protocol version $clientVersion does not match " +
                    "host protocol version $hostVersion"
            )
        }
    }
}
