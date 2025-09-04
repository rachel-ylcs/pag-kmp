import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "love.yinlin"
version = "1.0.0"

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm("desktop")

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "A library for integrating PAG (Portable Animated Graphics) with Compose Multiplatform."
        homepage = "https://github.com/rachel-ylcs/pag-kmp/"
        ios.deploymentTarget = "10.0"

        pod("libpag") {
            version = libs.versions.pag.get()
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        framework {
            baseName = "libpag_cmp"
            isStatic = true
        }

        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.library()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                api(project(":pag4j"))
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.pag.android)
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
                implementation(npm("libpag", "4.5.1")) // libs.versions.pag.get()
            }
        }
    }
}

android {
    namespace = "love.yinlin.libpag"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "libpag-compose", version.toString())

    pom {
        name = "libpag-compose"
        description = "A library for integrating PAG (Portable Animated Graphics) with Compose Multiplatform."
        inceptionYear = "2025"
        url = "https://github.com/rachel-ylcs/pag-kmp/"
        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://raw.githubusercontent.com/rachel-ylcs/pag-kmp/refs/heads/main/LICENSE"
            }
        }
        developers {
            developer {
                id = "ylcs"
                name = "银临茶舍"
                url = "https://github.com/rachel-ylcs/"
            }
        }
        scm {
            url = "https://github.com/rachel-ylcs/pag-kmp/"
            connection = "scm:git:git://github.com/rachel-ylcs/pag-kmp.git"
            developerConnection = "scm:git:ssh://git@ssh.github.com:443/rachel-ylcs/pag-kmp.git"
        }
    }
}
