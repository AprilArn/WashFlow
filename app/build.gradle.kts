import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

// Tambahkan kode ini untuk membaca file local.properties
val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}
// Ambil nilai API_KEY dari properties, beri nilai kosong jika tidak ada
val weatherApiKey = properties.getProperty("weather-api-key", "")

android {
    namespace = "com.aprilarn.washflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aprilarn.washflow"
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
        viewBinding = true
        compose = true
        buildConfig = true
    }
    flavorDimensions += "env"
    productFlavors {
        create("production"){
            buildConfigField(
                type = "String",
                name = "BASE_URL",
//                value = "\"https://api.openweathermap.org/data/2.5/\""
                value = "\"https://weather.googleapis.com/\""
            )
            buildConfigField(
                type = "String",
                name = "API_KEY",
                value = "$weatherApiKey"
            )
        }
        create("integration") {
            buildConfigField(
                type = "String",
                name = "BASE_URL",
//                value = "\"https://api.openweathermap.org/data/2.5/\""
                value = "\"https://weather.googleapis.com/\""
            )
            buildConfigField(
                type = "String",
                name = "API_KEY",
                value = "$weatherApiKey"
            )
        }
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
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.foundation)
    implementation(libs.material3)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.http.logging)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("androidx.compose.ui:ui-android:1.9.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    //implementation("com.google.firebase:firebase-auth-play-services:23.2.1")
    implementation(libs.firebase.firestore)

    // gms location
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // url image
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

}