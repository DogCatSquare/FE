plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)           // ← alias 사용
    alias(libs.plugins.hilt)
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

    // ✅ Java/Kotlin 17로 통일 (AGP 8.6 + Kotlin 2.0.x 권장)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // ✅ buildFeatures에서 한 번에 켜기
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
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
}

dependencies {
    // --- 기본 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Kotlin BOM으로 stdlib/reflect 버전 강제 정렬 → 2.0.20
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.20"))

    // --- 네이버 지도 SDK ---
    implementation("com.naver.maps:map-sdk:3.20.0")

    // --- Lifecycle (버전 중복 제거: 2.8.7로 통일) ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // --- Coroutines (Kotlin 2.0.x 호환) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // --- Retrofit / OkHttp (중복/구버전 제거, BOM 사용) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

    // --- Gson ---
    implementation(libs.gson)

    // --- Glide ---
    implementation(libs.glide)
    annotationProcessor(libs.compiler) // Glide 컴파일러는 annotationProcessor 유지

    // --- 위치/플렉스박스/Datastore ---
//    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // --- Room (KSP로 전환) ---
    implementation("androidx.room:room-runtime:2.7.0-rc01")
    implementation("androidx.room:room-ktx:2.7.0-rc01")
    ksp("androidx.room:room-compiler:2.7.0-rc01")

    // --- Hilt (KSP로 전환) ---
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")

    // 구글 지도
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
}
