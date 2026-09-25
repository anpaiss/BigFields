plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace  = "com.anpaiss.bigfields"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.anpaiss.bigfields"
        // 26: autosize del testo nei layout XML (android:autoSizeTextType).
        // Karoo 2 è Android 8.1 (27), Karoo 3 Android 12.
        minSdk        = 26
        targetSdk     = 34
        versionCode   = 1
        versionName   = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    testOptions {
        // I test puri leggono res/xml/extension_info.xml dal sorgente
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.hammerhead.karoo.ext)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
