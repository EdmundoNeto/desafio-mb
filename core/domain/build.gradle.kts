plugins {
    id("desafiomb.jvm.library")
    id("desafiomb.android.test")
    alias(libs.plugins.kover)
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "br.com.edmundo.desafiomb.core.domain.di.DomainModuleKt",
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
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
