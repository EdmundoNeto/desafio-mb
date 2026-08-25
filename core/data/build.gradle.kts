import java.util.Properties

plugins {
    id("desafiomb.android.library")
    id("desafiomb.android.test")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kover)
}

val cmcApiKey: String =
    Properties()
        .apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use { load(it) }
        }.getProperty("CMC_API_KEY")
        ?: System.getenv("CMC_API_KEY")
        ?: ""

android {
    namespace = "br.com.edmundo.desafiomb.core.data"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "CMC_API_KEY", "\"$cmcApiKey\"")
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "br.com.edmundo.desafiomb.core.data.BuildConfig",
                    "*_Impl",
                    "*_Impl\$*",
                )
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.junit)
}
