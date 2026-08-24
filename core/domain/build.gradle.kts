plugins {
    id("desafiomb.jvm.library")
    id("desafiomb.android.test")
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
