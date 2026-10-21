package com.example.android.features.menu.data.repository

import com.example.android.core.data.ErrorMapper
import com.example.android.core.domain.DomainException
import com.example.android.features.menu.FakeMenuApi
import com.example.android.features.menu.FakeMenuDao
import com.example.android.features.menu.domain.model.Menu
import com.example.android.features.menu.menuDto
import com.example.android.features.menu.menuEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MenuRepositoryImplTest {

    private fun httpError(code: Int, json: String) = HttpException(
        Response.error<Any>(code, json.toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun `observeMenu lee de Room y mapea a dominio en orden`() = runTest {
        val dao = FakeMenuDao(listOf(menuEntity("profile", 2), menuEntity("home", 1)))

        val menu = MenuRepositoryImpl(FakeMenuApi(), dao).observeMenu().first()

        assertEquals(
            listOf(
                Menu("home", "Texto home", "home", "app-taxi://passenger/home", 1),
                Menu("profile", "Texto profile", "home", "app-taxi://passenger/profile", 2)
            ),
            menu
        )
    }

    @Test
    fun `observeMenu no llama a la red`() = runTest {
        val api = FakeMenuApi()

        MenuRepositoryImpl(api, FakeMenuDao()).observeMenu().first()

        assertEquals(0, api.calls)
    }

    @Test
    fun `refreshMenu guarda en Room lo que responde el backend`() = runTest {
        val dao = FakeMenuDao()
        val api = FakeMenuApi { listOf(menuDto("home", 1), menuDto("profile", 2)) }

        MenuRepositoryImpl(api, dao, now = { 1_000L }).refreshMenu()

        assertEquals(
            listOf(menuEntity("home", 1, updatedAt = 1_000L), menuEntity("profile", 2, updatedAt = 1_000L)),
            dao.rows()
        )
    }

    @Test
    fun `refreshMenu elimina las opciones que el backend ya no envia`() = runTest {
        val dao = FakeMenuDao(listOf(menuEntity("home", 1), menuEntity("old", 2)))
        val api = FakeMenuApi { listOf(menuDto("home", 1)) }

        MenuRepositoryImpl(api, dao).refreshMenu()

        assertEquals(listOf("home"), dao.rows().map { it.id })
    }

    @Test
    fun `sin red refreshMenu lanza NetworkException y conserva la cache`() = runTest {
        val cached = listOf(menuEntity("home", 1))
        val dao = FakeMenuDao(cached)
        val repo = MenuRepositoryImpl(FakeMenuApi { throw IOException("unreachable") }, dao)

        val error = runCatching { repo.refreshMenu() }.exceptionOrNull()

        assertTrue(error is DomainException.NetworkException)
        assertEquals(ErrorMapper.MESSAGE_NETWORK, error?.message)
        assertEquals(cached, dao.rows())
    }

    @Test
    fun `401 del backend se traduce a UnauthorizedException con el mensaje del servidor`() = runTest {
        val body = """{"status_code":401,"message":"Tu sesión expiró. Inicia sesión nuevamente.","errors":[]}"""
        val repo = MenuRepositoryImpl(FakeMenuApi { throw httpError(401, body) }, FakeMenuDao())

        val error = runCatching { repo.refreshMenu() }.exceptionOrNull()

        assertTrue(error is DomainException.UnauthorizedException)
        assertEquals("Tu sesión expiró. Inicia sesión nuevamente.", error?.message)
    }

    @Test
    fun `la cancelacion no se convierte en error de dominio`() = runTest {
        val repo = MenuRepositoryImpl(FakeMenuApi { throw CancellationException("cancelado") }, FakeMenuDao())

        val error = runCatching { repo.refreshMenu() }.exceptionOrNull()

        assertTrue(error is CancellationException)
    }
}
