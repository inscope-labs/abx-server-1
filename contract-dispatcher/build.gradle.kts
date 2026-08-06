plugins {
  alias(libs.plugins.android.library)
  `maven-publish`
}

android {
  namespace = "com.inscopelabs.abx.server.contractdispatcher"
  compileSdk = 36

  defaultConfig {
    minSdk = 24
  }

  buildFeatures {
    aidl = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
}

publishing {
  publications {
    register<MavenPublication>("release") {
      groupId = "com.inscopelabs.abx.server"
      artifactId = "contract-dispatcher"
      version = "1.0.0"
      afterEvaluate {
        from(components["release"])
      }
    }
  }
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/inscope-labs/abx-server-1")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}

