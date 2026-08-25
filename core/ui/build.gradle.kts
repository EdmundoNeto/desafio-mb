plugins {
    id("desafiomb.android.library")
    id("desafiomb.android.compose")
}

android {
    namespace = "br.com.edmundo.desafiomb.core.ui"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
    api(libs.coil.compose)
    api(libs.coil.network.okhttp)
}
