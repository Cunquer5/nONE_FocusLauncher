plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Version constants
val COMPILE_SDK = 34
val MIN_SDK = 28
val TARGET_SDK = 34
val JAVA_VERSION = JavaVersion.VERSION_17

android {
    namespace = "com.example.nonelauncher"
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "com.example.nonelauncher"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Version catalogs for library dependencies
    val composeBom = "androidx.compose:compose-bom:2024.06.00"
    val roomVersion = "2.7.0-alpha11"

    // Core Android libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Lifecycle management
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Compose UI framework (managed via BOM)
    implementation(platform(composeBom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Data persistence
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Room database with Kotlin extensions
    // Updated to 2.7.0-alpha11 to fix "unexpected jvm signature V" with Kotlin 2.x
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Testing - Unit tests
    testImplementation("junit:junit:4.13.2")

    // Testing - Android Instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform(composeBom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
