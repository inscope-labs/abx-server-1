# Process Report: DispatcherRouter Timeout Fix & Test Coverage

**Timestamp (UTC):** 2026-08-06T03:02:00Z  
**Task:** Restructure `DispatcherRouter.route()` so `executor.execute(request)` is wrapped within `withTimeout(TIMEOUT_MS)`, and add `route_bindTimesOut_returnsFailClosedAndLogsAudit` test.

---

## 1. Code Changes

### A. Restructured `DispatcherRouter.kt`
- **Before:** `withTimeout(TIMEOUT_MS) { suspendCancellableCoroutine { ... } }.let { binder -> executor.execute(request) }`
  - *Issue:* `withTimeout` wrapped only the `suspendCancellableCoroutine` bind phase. If the cross-process `executor.execute(request)` call hung post-bind, it was not time-bounded.
- **After:**
  ```kotlin
  val response = withTimeout(TIMEOUT_MS) {
      val binder = suspendCancellableCoroutine { continuation ->
          // ServiceConnection & context.bindService logic...
      }
      val executor = IDispatcherExecutor.Stub.asInterface(binder)
      executor.execute(request)
  }
  ```
  - *Fix:* `executor.execute(request)` now executes within the same 10-second `withTimeout` scope, ensuring the entire round trip (bind + execute) is bounded.

### B. Added Unit Test in `DispatcherRouterTest.kt`
- Added `route_bindTimesOut_returnsFailClosedAndLogsAudit`:
  - Uses an anonymous `ContextWrapper` overriding `bindService` to return `true` without ever triggering `onServiceConnected`.
  - Verifies that `withTimeout(TIMEOUT_MS)` throws `TimeoutCancellationException` after timeout.
  - Verifies fail-closed return: `response.success == false`, `response.errorCode == null`, `response.errorMessage == "Dispatcher unreachable — request rejected"`.
  - Verifies `AuditLog` entry with `ReasonCode.DISPATCHER_UNREACHABLE`, `sessionId == "sess-3"`, and `details == "bind timeout"`.

---

## 2. Test & Build Verification

1. `compile_applet`: **SUCCESS**
2. `gradle :core:dispatcher:testDebugUnitTest`: **BUILD SUCCESSFUL** (All 3 tests passed: protocol mismatch, service not found, bind timeout).

---

## 3. Files Touched

- `core/dispatcher/src/main/java/com/inscopelabs/abx/server/core/dispatcher/DispatcherRouter.kt`
- `core/dispatcher/src/test/java/com/inscopelabs/abx/server/core/dispatcher/DispatcherRouterTest.kt`
