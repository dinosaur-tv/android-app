plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.dinotv.home"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.dinotv.home"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    // Release signing comes from the environment, never from Git. Without it assembleRelease stays unsigned.
    signingConfigs {
        create("release") {
            val store = providers.environmentVariable("DINO_KEYSTORE_PATH").orNull
            if (!store.isNullOrBlank()) {
                storeFile = file(store)
                storePassword = providers.environmentVariable("DINO_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("DINO_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("DINO_KEY_PASSWORD").orNull
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    lint { abortOnError = true }
}
dependencies { testImplementation("junit:junit:4.13.2") }
