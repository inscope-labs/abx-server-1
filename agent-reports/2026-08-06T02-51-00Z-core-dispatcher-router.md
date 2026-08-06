# Process Report: Core Dispatcher Router

**Timestamp (UTC):** 2026-08-06T02:51:00Z  
**Task:** Create new Gradle module `core:dispatcher` with `DispatcherRouter` binding to xtools dispatcher AIDL service and fail-closed error handling.

---

## 1. Summary of Work

### A. Created `core:dispatcher` Module
1. `core/dispatcher/build.gradle.kts`
   - Configured Android library with `namespace = "com.inscopelabs.abx.server.core.dispatcher"`, `compileSdk = 36`, `minSdk = 24`, Java 11.
   - Dependencies: `androidx.core.ktx`, `kotlinx.coroutines.core`, `:contract-dispatcher`, `:core:audit`, `junit`, `kotlinx.coroutines.test`, `robolectric`, `androidx.core`, `:core:keystore`.
2. `core/dispatcher/src/main/java/com/inscopelabs/abx/server/core/dispatcher/DispatcherRouter.kt`
   - `class DispatcherRouter(private val context: Context)`
   - `suspend fun route(request: DispatcherRequest): DispatcherResponse`
   - Checks `ProtocolVersionCheck.check()` from `:contract-dispatcher`. Returns mismatch response on protocol version failure.
   - Binds to AIDL service with `SERVICE_ACTION`, `TARGET_PACKAGE_NAME`, and `TARGET_SERVICE_CLASS_NAME`.
   - Uses `withTimeout(TIMEOUT_MS)` (10,000ms) and `suspendCancellableCoroutine` for asynchronous service binding.
   - Logs `ReasonCode.DISPATCHER_UNREACHABLE` to `AuditLog` on bind failure, timeout, or remote exception, and returns fail-closed `DispatcherResponse`.
3. `core/dispatcher/src/main/java/com/inscopelabs/abx/server/core/dispatcher/DispatcherRouterProvider.kt`
   - Singleton provider `DispatcherRouterProvider` with `@Synchronized get(context: Context)` and `setForTesting(router: DispatcherRouter?)`.
4. `core/dispatcher/src/test/java/com/inscopelabs/abx/server/core/dispatcher/DispatcherRouterTest.kt`
   - Unit tests covering protocol version mismatch and service unreachability / bind failure handling.

### B. Updated Constants & Audit Enums
5. `contract-dispatcher/src/main/java/com/inscopelabs/abx/server/contractdispatcher/DispatcherContractConstants.kt`
   - **Exact package path confirmed:** `package com.inscopelabs.abx.server.contractdispatcher`
   - Added:
     - `const val TARGET_PACKAGE_NAME: String = "com.inscopelabs.abx.xtools"`
     - `const val TARGET_SERVICE_CLASS_NAME: String = "com.inscopelabs.abx.xtools.dispatcher.DispatcherExecutorService"`
6. `core/audit/src/main/java/com/inscopelabs/abx/server/core/audit/AuditLog.kt`
   - Added `DISPATCHER_UNREACHABLE` entry to `ReasonCode` enum.

### C. Updated Settings
7. `settings.gradle.kts`
   - Added `include(":core:dispatcher")` directly after `include(":core:mcp")` and before `include(":contract")`.

---

## 2. Build & Test Verification

- `compile_applet`: **SUCCESS**
- `gradle :core:dispatcher:testDebugUnitTest`: **BUILD SUCCESSFUL** (Robolectric JVM unit tests passed)

---

## 3. Assumptions & Notes

- The target service class `com.inscopelabs.abx.xtools.dispatcher.DispatcherExecutorService` is forward-referenced for future xtools integration; current bind attempts correctly fail closed as designed.
- No local or in-process fallback execution is attempted on dispatch failure.
