import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.aeye"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.aeye"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    //Firebase
    implementation(libs.firebase.analytics)
    implementation(platform(libs.firebase.bom.v3320))
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics.buildtools)

    //Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //Compose
    implementation(platform(libs.androidx.compose.bom.v20260201))
    implementation(libs.ui)
    implementation(libs.androidx.ui.text)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.foundation)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)

    //Auth
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    //UI
    implementation(libs.coil.compose)

    //Networking / JSON
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.json)
    implementation(libs.kotlinx.coroutines.android)

    //Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)
    implementation(libs.places)
    implementation(libs.accompanist.permissions)

    //View System
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout.v213)
    implementation(libs.material)

    //Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)

    //Android Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.mockito.android)

    //Datastore
    implementation (libs.androidx.datastore.preferences)
}
