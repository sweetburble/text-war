buildscript {
    extra["hilt-android"] = "2.56.2"
    extra["hilt-navigation"] = "1.2.0"
    extra["Timber"] = "5.0.1"
    extra["lifecycle"] = "2.9.0"
    extra["coil"] = "2.5.0"
    extra["navigation"] = "2.9.0"

    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:${project.extra["hilt-android"]}")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.hilt.android) apply false

    // 코틀린 버전과 호환되는 KSP 버전 선택 필요
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}