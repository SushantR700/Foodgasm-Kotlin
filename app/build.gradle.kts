plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.foodgasm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodgasm"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding=true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //loading button
    implementation("br.com.simplepass:loading-button-android:2.2.0")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.13.0")

    //circular image
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //viewpager2 indicatior
    implementation("io.github.vejei.viewpagerindicator:viewpagerindicator:1.0.0-alpha.1")

    //stepView
    implementation("com.shuhart.stepview:stepview:1.5.1")

    //Android Ktx
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")

    //Material edittext
    implementation("com.google.android.material:material:1.11.0")

    //daggerhilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    //navigation-component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    //Imageslider
    implementation("com.github.denzcoskun:ImageSlideshow:0.1.2")

    //Picasso
    implementation("com.squareup.picasso:picasso:2.8")

    implementation ("com.google.code.gson:gson:2.10.1")

    //Coroutines with firebase
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    //Map
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps:google-maps-services:0.2.5")

    // khalti
    implementation("com.khalti:khalti-android:2.01.02")
}

