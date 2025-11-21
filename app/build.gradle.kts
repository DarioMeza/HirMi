plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // 👇 necesario para Room (procesador de anotaciones)
    id("kotlin-kapt")
}

android {
    namespace = "cl.example.hirmi"
    compileSdk = 36

    defaultConfig {
        applicationId = "cl.example.hirmi"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // === Navegación y Compose ===
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.foundation)
    implementation("androidx.navigation:navigation-compose:2.7.3")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // === ROOM (SQLite local) === 🧩
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // === (Opcional pero recomendado) ===
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // === Tests ===
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp Logging (para ver las peticiones/respuestas en Logcat)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // DataStore (para sesión persistente)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
