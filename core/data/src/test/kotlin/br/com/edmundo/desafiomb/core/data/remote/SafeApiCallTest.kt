package br.com.edmundo.desafiomb.core.data.remote

import br.com.edmundo.desafiomb.core.data.remote.dto.CmcResponse
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcStatus
import br.com.edmundo.desafiomb.core.domain.error.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class SafeApiCallTest {

    @Test
    fun `dado resposta com sucesso, quando safeApiCall, entao transforma o data e retorna DomainResult de sucesso`() = runTest {
        val response = CmcResponse(status = CmcStatus(errorCode = 0), data = "raw")

        val result = safeApiCall(call = { response }, transform = { it.uppercase() })

        assertEquals("RAW", result.getOrNull())
    }

    @Test
    fun `dado status com error_code diferente de zero, quando safeApiCall, entao retorna failure mapeado do CmcStatus`() = runTest {
        val response = CmcResponse<String>(status = CmcStatus(errorCode = 1001), data = null)

        val result = safeApiCall(call = { response }, transform = { it })

        assertEquals(AppError.InvalidApiKey, result.errorOrNull())
    }

    @Test
    fun `dado data nulo com status ok, quando safeApiCall, entao retorna failure Serialization`() = runTest {
        val response = CmcResponse<String>(status = CmcStatus(errorCode = 0), data = null)

        val result = safeApiCall(call = { response }, transform = { it })

        assertEquals(AppError.Serialization, result.errorOrNull())
    }

    @Test
    fun `dado excecao de rede, quando safeApiCall, entao retorna failure mapeado`() = runTest {
        val result = safeApiCall<String, String>(
            call = { throw UnknownHostException() },
            transform = { it },
        )

        assertEquals(AppError.NoConnection, result.errorOrNull())
    }

    @Test
    fun `dado CancellationException, quando safeApiCall, entao relanca sem converter em failure`() = runTest {
        var thrown = false
        try {
            safeApiCall<String, String>(
                call = { throw CancellationException("cancelled") },
                transform = { it },
            )
        } catch (e: CancellationException) {
            thrown = true
        }
        assertTrue(thrown)
    }
}
