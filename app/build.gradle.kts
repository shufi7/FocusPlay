plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

val freemodelApiKey = providers.gradleProperty("FREEMODEL_API_KEY")
    .orElse(providers.environmentVariable("FREEMODEL_API_KEY"))
    .getOrElse("")
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.example.focusplay"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.focusplay"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "FREEMODEL_API_KEY", "\"$freemodelApiKey\"")
    }

    buildFeatures {
        buildConfig = true
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
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
