import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm("desktop")

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    cocoapods {
        version = "1.0"
        summary = "libpag-compose example."
        homepage = "https://github.com/rachel-ylcs/pag-kmp/example"
        ios.deploymentTarget = "15.0"

        pod("libpag") {
            version = libs.versions.pag.get()
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
        podfile = project.file("iosApp/Podfile")
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.lifecycle.runtime.compose)
                implementation(project(":library"))
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.activity.compose)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }

        listOf(
            iosX64Main,
            iosArm64Main,
            iosSimulatorArm64Main
        ).forEach {
            it.get().dependsOn(iosMain)
        }

        val wasmJsMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(devNpm("node-polyfill-webpack-plugin", "*"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "love.yinlin.libpag.example.MainKt"
    }
}

tasks.withType<JavaExec> {
    val libraryPath = project(":pag4j").projectDir.resolve(".cxx")
    systemProperty("java.library.path", libraryPath)
}

android {
    compileSdk = 35
    namespace = "love.yinlin.libpag.example"

    defaultConfig {
        applicationId = "love.yinlin.libpag.example"
        minSdk = 24
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
}
