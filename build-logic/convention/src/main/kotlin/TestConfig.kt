import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/**
 * O Gradle 9 falha a task de teste quando o modulo tem source set de teste mas
 * nenhum teste descoberto. Modulos ainda sem testes (E1) fariam o `build` falhar
 * por configuracao, nao por qualidade.
 *
 * A garantia real de cobertura e o gate do Kover (spec 9.2, >= 80% em :core:domain
 * e :core:data) e o KoinModulesTest bloqueante no CI (spec 9.3) - nao esta flag.
 */
internal fun Project.configureTests() {
    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests.set(false)
    }
}
