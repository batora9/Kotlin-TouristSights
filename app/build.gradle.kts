plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.9.0"
}

// .envファイルからAPIキーを読み込む
val envFile = rootProject.file(".env")
val googleMapsApiKey = if (envFile.exists()) {
    val envContent = envFile.readText()
    val apiKeyLine = envContent.lines().find { it.startsWith("GOOGLE_MAPS_API_KEY=") }
    apiKeyLine?.substringAfter("=") ?: ""
} else {
    // .envファイルが存在しない場合はエラー表示
    throw GradleException(".envファイルが存在しません")
}

android {
    namespace = "com.example.touristsights"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.touristsights"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // .envファイルからGoogle Maps API Keyをマニフェストプレースホルダーに設定
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation(libs.androidx.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}