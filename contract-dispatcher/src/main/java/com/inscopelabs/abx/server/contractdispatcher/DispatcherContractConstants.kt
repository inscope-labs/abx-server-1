package com.inscopelabs.abx.server.contractdispatcher

object DispatcherContractConstants {
    const val SIGNATURE_PERMISSION: String =
        "com.inscopelabs.abx.permission.DISPATCHER_EXECUTION"
    const val PROTOCOL_VERSION: Int = 1
    const val SERVICE_ACTION: String =
        "com.inscopelabs.abx.contractdispatcher.DISPATCHER_EXECUTOR_SERVICE"
    const val ERROR_CODE_PROTOCOL_VERSION_MISMATCH: Int = 1001
    const val TARGET_PACKAGE_NAME: String = "com.inscopelabs.abx.xtools"
    const val TARGET_SERVICE_CLASS_NAME: String =
        "com.inscopelabs.abx.xtools.dispatcher.DispatcherExecutorService"
}
