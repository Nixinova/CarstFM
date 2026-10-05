plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    androidLibrary {
        namespace = "com.nixinova.carstfm.shared"
        compileSdk = 37
        minSdk = 33
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
        }

        androidMain {
            kotlin.srcDir("src/commonMain/java")
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }
    }
}
