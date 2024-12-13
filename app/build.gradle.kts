plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.fpmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fpmobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Room database
    implementation ("androidx.room:room-runtime:2.5.2")
    annotationProcessor ("androidx.room:room-compiler:2.5.2")
    // AndroidX dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // TensorFlow Lite core library
    implementation("org.tensorflow:tensorflow-lite:2.13.0")

    // Optional: TensorFlow Lite Select TensorFlow Operations
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.13.0")

    // Optional: TensorFlow Lite GPU delegate for acceleration
    implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}