import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

// Credenziali firma release da local.properties (mai committato), stesso keystore
// delle altre estensioni: signing.storeFile / storePassword / keyAlias / keyPassword
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace  = "com.anpaiss.bigfields"
    compileSdk = 34

    signingConfigs {
        if (localProps.containsKey("signing.storeFile")) {
            create("release") {
                storeFile     = file(localProps["signing.storeFile"] as String)
                storePassword = localProps["signing.storePassword"] as String
                keyAlias      = localProps["signing.keyAlias"] as String
                keyPassword   = localProps["signing.keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.anpaiss.bigfields"
        // 26: autosize del testo nei layout XML (android:autoSizeTextType).
        // Karoo 2 è Android 8.1 (27), Karoo 3 Android 12.
        minSdk        = 26
        targetSdk     = 34
        versionCode   = 95
        versionName   = "0.95-beta"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
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
