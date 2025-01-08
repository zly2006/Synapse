import de.jensklingenberg.gradle.TestCompilerExtension

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin
    application
}
apply(plugin = "compiler.gradleplugin.helloworld")

configure<TestCompilerExtension> {
    enabled = true
}

dependencies {
    // ktx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.compileKotlin {
    outputs.upToDateWhen { false }
}

//application {
//    mainClassName = "de.jensklingenberg.gradle.MainKt"
//}
