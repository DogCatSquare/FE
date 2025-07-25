import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.dogcatsquare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dogcatsquare"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${project.findProperty("naver.client.id") ?: ""}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${project.findProperty("naver.client.secret") ?: ""}\"")
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
    viewBinding {
        enable = true
    }
    dataBinding {
        enable = true
    }

    packaging {
        resources {
            excludes += ("/META-INF/INDEX.LIST")
            excludes += ("/META-INF/DEPENDENCIES")
            excludes += ("/META-INF/LICENSE")
            excludes += ("/META-INF/LICENSE.txt")
            excludes += ("/META-INF/license.txt")
            excludes += ("/META-INF/NOTICE")
            excludes += ("/META-INF/NOTICE.txt")
            excludes += ("/META-INF/notice.txt")
            excludes += ("/META-INF/*.kotlin_module")
        }
    }

    buildFeatures {
        buildConfig = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.appdistribution.gradle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 네이버 지도 SDK
    implementation("com.naver.maps:map-sdk:3.20.0")

    //viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // json <-> 객체 변환하기 위해 사용
    implementation(libs.gson)

    //retrofit 네트워크 라이브러리 관련
    implementation(libs.converter.gson)
    implementation(libs.retrofit)

    // material design
    implementation (libs.material.v190)

    // Glide
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // OkHttp
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")
}