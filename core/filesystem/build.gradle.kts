plugins {
  alias(libs.plugins.android.library)
}
android {
  namespace = "com.inscopelabs.abx.server.core.filesystem"
  compileSdk = 36
  defaultConfig { minSdk = 24 }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}
dependencies {
  implementation(libs.androidx.core.ktx)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.core)
}
