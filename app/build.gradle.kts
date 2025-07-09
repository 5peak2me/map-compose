import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
    id("kotlin-parcelize")
    kotlin("plugin.serialization") version "2.2.0"
    id("elf-16k-alignment")
}

elfAlignment {
    filter = false

    maxAlign = 16384L

    output {
        csv = true
        html = true
        json = true
        md = true
    }
}

android {
    namespace = "com.github.speak2me.app.compose.map"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.speak2me.app.compose.map"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a"/*, "armeabi-v7a"*/, "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        named("debug") {
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
    }
    sourceSets.all {
        // Must be explicitly enabled!
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
    sourceSets.all {
        // Must be explicitly enabled!
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.1.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    implementation(project(":maps:amap"))
    implementation(project(":maps:baidu"))
    implementation(project(":maps:tencent"))

    implementation(libs.map.google)
    // https://play.google.com/sdks/details/com-google-android-gms-play-services-location?hl=zh-CN
    implementation(libs.map.google.location)
    implementation(libs.map.google.utils)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.compose)

    // Accompanist Permissions
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

//secrets {
//    // To add your Maps API key to this project:
//    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
//    // 2. Add this line, where YOUR_API_KEY is your API key:
//    //        MAPS_API_KEY=YOUR_API_KEY
//    propertiesFileName = "secrets.properties"
//
//    // A properties file containing default secret values. This file can be
//    // checked in version control.
//    defaultPropertiesFileName = "local.defaults.properties"
//}
