plugins {
    id("desafiomb.android.library")
}

android {
    namespace = "br.com.edmundo.desafiomb.core.testing"
}

dependencies {
    api(project(":core:domain"))

    api(platform(libs.koin.bom))
    api(libs.koin.test)
    api(libs.koin.test.junit4)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
