// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("kotlin-kapt") apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
