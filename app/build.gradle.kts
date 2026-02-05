plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tellymobile"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.tellymobile"
        minSdk = 26
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

    androidResources {
        ignoreAssetsPatterns.addAll(
            listOf(
                "!.svn",
                "!.git",
                "!.ds_store",
                "!*.scc",
                ".*",
                "!CVS",
                "!thumbs.db",
                "!picasa.ini",
                "!*~",
                "!~$*"
            )
        )
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.mpandroidchart)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
}



