plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // ✅ Plugin Google Services
}

android {
    namespace = "com.example.slidingpuzzlegame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.slidingpuzzlegame"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {
    // ✅ Firebase BoM: Mengelola semua versi library Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))

    // ✅ Tambahkan layanan Firebase yang dibutuhkan
    implementation("com.google.firebase:firebase-analytics") // Opsional
    implementation("com.google.firebase:firebase-auth") // ⬅️ WAJIB untuk login/registrasi Firebase
    implementation("com.google.firebase:firebase-firestore") // ⬅️ WAJIB untuk simpan skor ke Firestore

    // ✅ UI Komponen
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")



}
