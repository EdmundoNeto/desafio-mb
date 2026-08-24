package br.com.edmundo.desafiomb.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleDependencyTest {

    private val repoRoot: File = generateSequence(File(".").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    @Test
    fun `dado um modulo de feature, quando inspecionado, entao nao depende de core data`() {
        val featureDir = File(repoRoot, "feature")
        val buildScripts = featureDir.walkTopDown()
            .filter { it.name == "build.gradle.kts" }
            .toList()

        assertTrue("Nenhum modulo de feature encontrado em ${featureDir.absolutePath}", buildScripts.isNotEmpty())

        buildScripts.forEach { script ->
            val offending = script.readLines()
                .map { it.substringBefore("//").trim() }
                .filter { it.contains("\":core:data\"") }

            assertTrue(
                "RNF-03 violado: ${script.relativeTo(repoRoot)} declara dependencia em :core:data -> $offending",
                offending.isEmpty(),
            )
        }
    }

    @Test
    fun `dado core domain, quando inspecionado, entao nao usa plugin android`() {
        val script = File(repoRoot, "core/domain/build.gradle.kts").readText()
        assertFalse(
            "core:domain deve ser Kotlin puro (JVM), sem plugin Android (spec 2.2)",
            script.contains("desafiomb.android.library") || script.contains("com.android"),
        )
    }
}
