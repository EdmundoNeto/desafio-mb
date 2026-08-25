plugins {
    id("desafiomb.android.application")
    id("desafiomb.android.compose")
    id("desafiomb.android.test")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "br.com.edmundo.desafiomb.app"

    defaultConfig {
        applicationId = "br.com.edmundo.desafiomb"
        versionCode = 3
        versionName = "1.0.2"
        testInstrumentationRunner = "br.com.edmundo.desafiomb.app.NoKoinTestRunner"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:exchanges"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)

    testImplementation(project(":core:testing"))

    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(platform(libs.koin.bom))
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.koin.android.test) {
        exclude(group = "io.insert-koin", module = "koin-androidx-workmanager")
    }
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
