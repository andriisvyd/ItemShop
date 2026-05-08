// Top-level build file where you can add configuration options common to all sub-projects/modules.
//
// AGP 9.x ships with built-in Kotlin support, so we deliberately do NOT apply
// `org.jetbrains.kotlin.android` on Android modules: AGP pre-registers the
// `kotlin` Gradle extension, and re-applying that plugin throws
// "Cannot add extension with name 'kotlin'". Pure-JVM modules (e.g. :domain)
// still need `kotlin.jvm`.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
