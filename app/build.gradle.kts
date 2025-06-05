import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")

    // Supabase 직렬화 플러그인
    kotlin("plugin.serialization") version "2.0.21"
}


fun getLocalProperty(key: String, project: Project): String {
    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(FileInputStream(localPropertiesFile))
    }
    return properties.getProperty(key, "")
}

android {
    namespace = "com.bandi.textwar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bandi.textwar"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase 설정
        buildConfigField("String", "SUPABASE_URL", "\"${getLocalProperty("SUPABASE_URL", project)}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${getLocalProperty("SUPABASE_ANON_KEY", project)}\"")
        buildConfigField("String", "OPENAI_API_KEY", "\"${getLocalProperty("OPENAI_API_KEY", project)}\"")
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
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Timber(로깅)
    implementation(libs.timber)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Supabase
    implementation(libs.supabase)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // ktor 안드로이드
    implementation(libs.ktor.android)
    implementation(libs.ktor.core)
    implementation(libs.ktor.utils)

    // Ktor Client CIO Engine (HTTP 요청용)
    implementation(libs.ktor.cio)
    // Ktor Content Negotiation (JSON 등 컨텐츠 협상용)
    implementation(libs.ktor.content.negotiation)
    // Ktor Kotlinx Serialization (JSON 직렬화/역직렬화용)
    implementation(libs.ktor.serialization.json)

    // OpenAI SDK
    implementation("com.aallam.openai:openai-client:4.0.1")

    // Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Arrow 패키지 (either 사용)
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    // Icon-Extended
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
}