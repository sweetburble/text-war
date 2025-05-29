// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // hilt
    alias(libs.plugins.hilt.android) apply false

    // 코틀린 버전과 호환되는 KSP 버전 선택 필요
    alias(libs.plugins.ksp) apply false
}