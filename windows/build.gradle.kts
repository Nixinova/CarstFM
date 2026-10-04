plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

dependencies {
    implementation(compose.desktop.currentOs)
}

kotlin {
    jvmToolchain(24)
}

compose.desktop {
    application {
        mainClass = "com.nixinova.carstfm.windows.WindowsMainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "CarstFM"
            packageVersion = "1.0.3"
        }
    }
}
