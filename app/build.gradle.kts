plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.example.waslniiii"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.waslniiii"
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
}
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // CardView est déjà inclus dans Material, pas besoin de l'ajouter
}
