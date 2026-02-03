plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.karwaan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.karwaan"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SUPABASE_URL", "\"https://ryeqemimspkgxhlzvaay.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_FKLnd95FRcOd0doozu6Vyw_uSeoGVVq\"")

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Activity + Compose
    implementation("androidx.activity:activity-compose:1.9.0")

    // Jetpack Compose BOM (ONE only)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended")

    // MapLibre
    implementation("org.maplibre.gl:android-sdk:11.8.0")

    // Debug / Testing
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.4.3")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.4.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.ktor:ktor-client-okhttp:2.3.7")

}
