// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP 9+ trae soporte a Kotlin integrado: https://developer.android.com/build/migrate-to-built-in-kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}