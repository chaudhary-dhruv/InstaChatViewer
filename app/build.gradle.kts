plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.dhruv.instachatviewer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dhruv.instachatviewer"
        minSdk = 26
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 🔥 Material Components (needed for Theme.MaterialComponents)
    implementation("com.google.android.material:material:1.12.0")

    // AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Material3 (for optional new widgets)
    implementation("androidx.compose.material3:material3:1.4.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Room Database
    implementation("androidx.room:room-runtime:2.8.1")
    kapt("androidx.room:room-compiler:2.8.1")
    implementation("androidx.room:room-ktx:2.8.1")

    // Gson (for JSON parsing)
    implementation("com.google.code.gson:gson:2.13.2")

    // Glide (for images & thumbnails)
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("com.github.bumptech.glide:compiler:5.0.5")

    // Lottie for loading animations
    implementation("com.airbnb.android:lottie:6.6.10")

    // File utils (optional but helpful)
    implementation("commons-io:commons-io:2.20.0")
}
