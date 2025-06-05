import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "love.yinlin.pag"
version = "1.0.0"

enum class GradlePlatform {
    Windows, Linux, Mac;

    override fun toString(): String = when (this) {
        Windows -> "win"
        Linux -> "linux"
        Mac -> "mac"
    }
}

val desktopPlatform = System.getProperty("os.name").let { when {
    it.lowercase().startsWith("windows") -> GradlePlatform.Windows
    it.lowercase().startsWith("mac") -> GradlePlatform.Mac
    else -> GradlePlatform.Linux
} }

val desktopArchitecture = System.getProperty("os.arch").let { when {
    it.lowercase().startsWith("aarch64") -> "aarch64"
    it.lowercase().startsWith("arm") -> "arm"
    it.lowercase().startsWith("amd64") -> "x86_64"
    else -> it
} }!!

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm("desktop")
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    iosArm64()
    if (desktopPlatform == GradlePlatform.Mac) {
        if (desktopArchitecture == "aarch64") iosSimulatorArm64() else iosX64()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {}
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)

                implementation(libs.kotlinx.coroutines)
            }
        }

        androidMain.get().apply {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.pag.android)
            }
        }
    }
}

android {
    namespace = "love.yinlin.pag"
    compileSdk = 35
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "library", version.toString())

    pom {
        name = "My library"
        description = "A library."
        inceptionYear = "2024"
        url = "https://github.com/kotlin/multiplatform-library-template/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}
