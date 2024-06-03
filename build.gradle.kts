// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    // Añadida dependencia para el plugin Gradle de Google services
    id("com.google.gms.google-services") version "4.4.2" apply false
}