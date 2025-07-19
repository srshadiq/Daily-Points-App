plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dailygoalpoints"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dailygoalpoints"
        minSdk = 26
        targetSdk = 35
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
    // Add MPAndroidChart for graphs
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // ViewPager2 for page navigation
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    // Fragment support
    implementation("androidx.fragment:fragment:1.6.2")
    // CardView
    implementation("androidx.cardview:cardview:1.0.0")
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}