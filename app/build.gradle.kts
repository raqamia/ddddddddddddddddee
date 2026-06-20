plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.manara"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Supabase configuration. The anon key is a public client key (protected by
    // Row Level Security on the server) and is safe to ship in the APK.
    buildConfigField("String", "SUPABASE_URL", "\"https://vdqrcshhnkqnqwrldjos.supabase.co\"")
    buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZkcXJjc2hobmtxbnF3cmxkam9zIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODEzNTE0MTUsImV4cCI6MjA5NjkyNzQxNX0.reAiDOnDFuWGFC2GSAzlDMJl1JN_woBkRWAOtqzTKZI\"")
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    viewBinding = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)
  
  implementation(libs.androidx.viewpager2)
  
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.lifecycle.livedata)
  
  implementation(libs.retrofit)
  implementation(libs.converter.gson)
  implementation(libs.okhttp)
  implementation(libs.logging.interceptor)
  
  implementation(libs.androidx.room.runtime)
  "ksp"(libs.androidx.room.compiler)
  
  implementation(libs.glide)
  "ksp"(libs.glide.compiler)
  
  implementation(libs.android.pdf.viewer)
  
  implementation(libs.androidx.security.crypto)
  
  implementation(libs.androidx.swiperefreshlayout)
  implementation(libs.androidx.core.splashscreen)
}
