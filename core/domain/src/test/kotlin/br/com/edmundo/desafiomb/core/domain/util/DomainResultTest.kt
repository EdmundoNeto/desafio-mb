package br.com.edmundo.desafiomb.core.domain.util

import br.com.edmundo.desafiomb.core.domain.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainResultTest {
    @Test
    fun `dado success, quando isSuccess e isFailure, entao reflete o estado de sucesso`() {
        val result = DomainResult.success(42)

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun `dado failure, quando isSuccess e isFailure, entao reflete o estado de falha`() {
        val result = DomainResult.failure(AppError.NoConnection)

        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
    }

    @Test
    fun `dado success, quando getOrNull e errorOrNull, entao retorna o valor e nulo para o erro`() {
        val result = DomainResult.success(42)

        assertEquals(42, result.getOrNull())
        assertNull(result.errorOrNull())
    }

    @Test
    fun `dado failure, quando getOrNull e errorOrNull, entao retorna nulo para o valor e o erro`() {
        val result = DomainResult.failure(AppError.Server)

        assertNull(result.getOrNull())
        assertEquals(AppError.Server, result.errorOrNull())
    }

    @Test
    fun `dado success, quando map, entao transforma o valor`() {
        val result = DomainResult.success(2).map { it * 10 }

        assertEquals(20, result.getOrNull())
    }

    @Test
    fun `dado failure, quando map, entao preserva o erro sem transformar`() {
        val result = DomainResult.failure(AppError.Timeout).map { it }

        assertEquals(AppError.Timeout, result.errorOrNull())
    }

    @Test
    fun `dado success, quando fold, entao executa apenas onSuccess`() {
        val result = DomainResult.success(2)

        val folded = result.fold(onSuccess = { "valor $it" }, onFailure = { "erro" })

        assertEquals("valor 2", folded)
    }

    @Test
    fun `dado failure, quando fold, entao executa apenas onFailure`() {
        val result = DomainResult.failure(AppError.Server)

        val folded = result.fold(onSuccess = { "valor" }, onFailure = { "erro $it" })

        assertEquals("erro ${AppError.Server}", folded)
    }

    @Test
    fun `dado success, quando onSuccess e onFailure, entao apenas onSuccess executa`() {
        var successCalled = false
        var failureCalled = false

        DomainResult
            .success(1)
            .onSuccess { successCalled = true }
            .onFailure { failureCalled = true }

        assertTrue(successCalled)
        assertFalse(failureCalled)
    }

    @Test
    fun `dado failure, quando onSuccess e onFailure, entao apenas onFailure executa`() {
        var successCalled = false
        var failureCalled = false

        DomainResult
            .failure(AppError.Server)
            .onSuccess { successCalled = true }
            .onFailure { failureCalled = true }

        assertFalse(successCalled)
        assertTrue(failureCalled)
    }
}
