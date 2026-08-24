import org.gradle.api.Plugin
import org.gradle.api.Project

/** Modulo Kotlin puro (JVM), sem Android - usado por :core:domain (spec 2.2). */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        configureKotlinJvm()
        configureTests()
    }
}
