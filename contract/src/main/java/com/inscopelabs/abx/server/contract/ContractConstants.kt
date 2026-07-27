package com.inscopelabs.abx.server.contract

object ContractConstants {
    const val SIGNATURE_PERMISSION: String =
        "com.inscopelabs.abx.permission.CAPABILITY_EXECUTION"
    const val PROTOCOL_VERSION: Int = 1
    const val SERVICE_ACTION: String =
        "com.inscopelabs.abx.contract.CAPABILITY_EXECUTOR_SERVICE"
    const val ERROR_CODE_PROTOCOL_VERSION_MISMATCH: Int = 1001
}
