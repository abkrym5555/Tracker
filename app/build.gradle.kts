plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.legacy.kapt)
}

android {
    namespace = "com.example.tracker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.tracker"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        // Room 2.6.1's bundled kotlinx-metadata only understands metadata up to 2.0.0
        freeCompilerArgs.add("-Xmetadata-version=2.0.0")
    }
}

// Room 2.6.1's bundled kotlinx-metadata only understands Kotlin metadata up to
// 2.0.0, so the compile/kapt classpaths get an older stdlib whose metadata is
// readable by it. The packaged app still resolves the newer stdlib at runtime.
configurations
    .matching { it.name.endsWith("CompileClasspath") || it.name.startsWith("kapt") }
    .configureEach {
        resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
        // Room probes `androidx.lifecycle.LiveData` for every suspend DAO method.
        // LiveData ships with Kotlin 2.1 metadata (unreadable by Room 2.6.1) and
        // this app does not use it, so keep it off the compile/kapt classpath only.
        exclude(group = "androidx.lifecycle", module = "lifecycle-livedata-core")
        exclude(group = "androidx.lifecycle", module = "lifecycle-livedata")
        exclude(group = "androidx.lifecycle", module = "lifecycle-livedata-ktx")
    }

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}