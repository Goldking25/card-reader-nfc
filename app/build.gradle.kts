plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// ── Signing helpers (outside android block, available everywhere) ─────────────
// Reads signing credentials in this priority order:
//   1. Gradle properties injected by CI:  -Pandroid.injected.signing.*
//   2. local.properties on developer machine  (DO NOT COMMIT this file)
//
// local.properties example:
//   signing.storeFile=../nfc_poc_key.jks
//   signing.storePassword=yourpassword
//   signing.keyAlias=nfc_poc
//   signing.keyPassword=yourpassword

val localProps = java.util.Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}
fun signingProp(name: String): String? =
    findProperty("android.injected.signing.$name")?.toString()
        ?: localProps.getProperty("signing.$name")

android {
    namespace = "com.nfcpoc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nfcpoc"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val sf = signingProp("storeFile")
            if (sf != null) {
                storeFile     = file(sf)
                storePassword = signingProp("storePassword")
                keyAlias      = signingProp("keyAlias")
                keyPassword   = signingProp("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true   // Required in AGP 8+ to generate BuildConfig.DEBUG
    }

    lint {
        // Don't abort the build on lint errors — report them but keep going.
        // This prevents CI from failing on warnings while still surfacing issues.
        abortOnError = false
        checkReleaseBuilds = false   // faster CI — full lint runs manually
        xmlReport = true             // needed for CI artifact upload
        htmlReport = true
        warningsAsErrors = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Room KSP: schema export directory
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Lifecycle (ViewModel + LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ── Unit Tests ───────────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("com.google.code.gson:gson:2.10.1")

    // ── Android Instrumented Tests ────────────────────────────────────────────
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("app.cash.turbine:turbine:1.1.0")
    androidTestImplementation("com.google.truth:truth:1.4.2")
}
