plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nixinova.carstfm"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.nixinova.carstfm"
        minSdk = 33
        targetSdk = 37
        versionName = providers.gradleProperty("appVersionNumber").get()
        versionCode = providers.gradleProperty("appVersionCode").get().toInt()
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(platform(libs.androidx.compose.bom))
    // Car app
    implementation(libs.androidx.app)
    // Compose UI
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.app.projected)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
