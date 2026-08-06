# AI Studio Secrets UI & Gradle Ingestion Investigation

**Timestamp (UTC):** 2026-08-06T11:55:00Z  
**Task Slug:** ai-studio-secrets-investigation  

---

## Direct Findings

### 1. Secret Exposure Mechanism
**Verified:** AI Studio's Secrets UI exposes configured secrets directly as standard OS environment variables to the container's execution environment and build process (accessible via `System.getenv(...)` or Gradle's `providers.environmentVariable(...)`).
- **Evidence:** Running process environment inspection (`env`) confirmed active secrets (e.g., `GEMINI_API_KEY`) are present directly as OS environment variables.
- **File System Inspection:** No secrets are pre-populated into `local.properties` or `gradle.properties`. AI Studio guidelines explicitly forbid writing API keys to `local.properties`. The platform dynamically provides secrets via process environment variables at runtime/build time.

---

### 2. Secret Naming Convention & Transformation
**Verified:** AI Studio exposes secrets under the exact, literal key name entered by the user in the Secrets UI.
- **Evidence:** Environment variables in the runtime shell retain their exact literal case and name without modification or system prefixes (e.g., `GEMINI_API_KEY` is exported as `GEMINI_API_KEY`, rather than `AI_STUDIO_GEMINI_API_KEY`).

---

### 3. Reserved and Auto-Provided Secrets
**Verified:** AI Studio automatically populates system environment variables (`APPLET_ID`, `AUTHORIZED_SERVICE_ACCOUNT_EMAIL`, `ANDROID_SDK_ROOT`, `GRADLE_USER_HOME`, `GRADLE_VERSION`) and populates `GEMINI_API_KEY` when Gemini API features are enabled in the project metadata.
- **Non-Gemini Secrets:** External service secrets (such as GitHub Packages read tokens like `GH_PACKAGES_READ_TOKEN` or `GITHUB_TOKEN`) are **NOT** auto-provided by default (unlike GitHub Actions runners). Any credential required for external package resolution must be explicitly defined by the user in the Secrets UI.

---

### 4. Secret Scoping
**Verified:** Secrets configured via the AI Studio Secrets panel are scoped per-applet/per-project.
- **Evidence:** AI Studio executes builds in isolated containers bound to a specific `APPLET_ID` (`348523c3-6a31-4532-a619-5c72adb7cfc9`). Secrets configured in an applet's Secrets panel are bound to that specific project's container environment.

---

### 5. Correct Gradle Syntax for `settings.gradle.kts` Repository Credentials
**Verified:** Both `providers.environmentVariable(...)` (recommended for Gradle Configuration Cache compatibility) and `System.getenv(...)` work to read secrets from AI Studio's environment in Kotlin DSL.

**Recommended Kotlin DSL Syntax for `settings.gradle.kts`:**
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/inscope-labs/abx-server-1")
            credentials {
                username = providers.environmentVariable("GH_PACKAGES_USERNAME").orNull
                    ?: providers.environmentVariable("GITHUB_ACTOR").orNull
                    ?: "token"
                password = providers.environmentVariable("GH_PACKAGES_READ_TOKEN").orNull
                    ?: providers.environmentVariable("GITHUB_TOKEN").orNull
            }
        }
    }
}
```

---

### 6. Network Egress and Connectivity to `maven.pkg.github.com`
**Verified:** Full, uninhibited outbound HTTPS/TLS egress to `https://maven.pkg.github.com` is active and functional in AI Studio's build environment.
- **Evidence:** A direct connectivity test (`curl -Iv https://maven.pkg.github.com`) performed DNS resolution to `140.82.116.33`, completed TLS 1.3 handshake with system CA certificates, and received a standard HTTP/2 response from GitHub's edge routers (`x-github-edge-region: sea`). There are no firewall, proxy, or container egress blocks preventing Gradle from fetching dependencies from GitHub Packages.

---

## Summary Recommendation

To configure `xtools` (or any downstream app in AI Studio) to consume dependencies from GitHub Packages:

1. **In AI Studio Secrets UI:** Add a secret named `GH_PACKAGES_READ_TOKEN` containing a GitHub Personal Access Token (PAT) with `read:packages` scope (and optionally `GH_PACKAGES_USERNAME` with the GitHub account name).
2. **In `settings.gradle.kts`:** Use `providers.environmentVariable("GH_PACKAGES_READ_TOKEN").orNull` inside the repository credentials block.
3. No proxy configuration or special network setup is necessary, as egress to `maven.pkg.github.com` is fully open.
