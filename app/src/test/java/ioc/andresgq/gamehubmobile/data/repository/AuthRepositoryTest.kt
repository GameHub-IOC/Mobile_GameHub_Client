package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.local.TokenManager
import ioc.andresgq.gamehubmobile.data.local.UserSessionDao
import ioc.andresgq.gamehubmobile.data.local.UserSessionEntity
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.data.remote.AuthApi
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterResponseDto
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthRepositoryTest {

    @Test
    fun login_success_mapsAndStoresSession() = runTest {
        val fakeApi = FakeAuthApi().apply {
            loginResult = LoginResponseDto("token-login", "usuario", "USER")
        }
        val fakeDao = FakeUserSessionDao()
        val repository = AuthRepository(fakeApi, TokenManager(fakeDao))

        val result = repository.login("usuario", "secret")

        assertTrue(result.isSuccess)
        assertEquals(
            UserSession("token-login", "usuario", "USER"),
            result.getOrNull()
        )
        assertEquals(LoginRequestDto("usuario", "secret"), fakeApi.lastLoginRequest)
        assertEquals(
            UserSessionEntity(0, "token-login", "usuario", "USER"),
            fakeDao.storedSession
        )
    }

    @Test
    fun login_httpException_returnsExpectedErrorMessage() = runTest {
        val fakeApi = FakeAuthApi().apply {
            loginThrowable = httpException(401)
        }
        val repository = AuthRepository(fakeApi, TokenManager(FakeUserSessionDao()))

        val result = repository.login("usuario", "bad-password")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("401") == true)
    }

    @Test
    fun register_409_returnsBusinessMessage() = runTest {
        val fakeApi = FakeAuthApi().apply {
            registerThrowable = httpException(409)
        }
        val repository = AuthRepository(fakeApi, TokenManager(FakeUserSessionDao()))

        val result = repository.register("usuario", "123456")

        assertTrue(result.isFailure)
        assertEquals(
            "Este nombre de usuario ya existe",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun register_ioException_returnsConnectivityMessage() = runTest {
        val fakeApi = FakeAuthApi().apply {
            registerThrowable = IOException("network down")
        }
        val repository = AuthRepository(fakeApi, TokenManager(FakeUserSessionDao()))

        val result = repository.register("usuario", "123456")

        assertTrue(result.isFailure)
        assertEquals(
            "No se pudo conectar con el servidor",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun logout_clearsStoredSession() = runTest {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token", "u", "USER")
        }
        val repository = AuthRepository(FakeAuthApi(), TokenManager(fakeDao))

        repository.logout()

        assertEquals(1, fakeDao.clearCalls)
        assertEquals(null, fakeDao.storedSession)
    }

    private fun httpException(code: Int): HttpException {
        val response = Response.error<Any>(code, "error".toResponseBody())
        return HttpException(response)
    }

    private class FakeAuthApi : AuthApi {
        var loginResult: LoginResponseDto = LoginResponseDto("token", "name", "USER")
        var registerResult: RegisterResponseDto = RegisterResponseDto(0L, "name", "USER")
        var loginThrowable: Throwable? = null
        var registerThrowable: Throwable? = null

        var lastLoginRequest: LoginRequestDto? = null
        var lastRegisterRequest: RegisterRequestDto? = null

        override suspend fun login(request: LoginRequestDto): LoginResponseDto {
            lastLoginRequest = request
            loginThrowable?.let { throw it }
            return loginResult
        }

        override suspend fun register(request: RegisterRequestDto): RegisterResponseDto {
            lastRegisterRequest = request
            registerThrowable?.let { throw it }
            return registerResult
        }
    }

    private class FakeUserSessionDao : UserSessionDao {
        var storedSession: UserSessionEntity? = null
        var clearCalls: Int = 0

        override suspend fun getSession(): UserSessionEntity? = storedSession

        override suspend fun upsert(session: UserSessionEntity) {
            storedSession = session
        }

        override suspend fun clear() {
            clearCalls += 1
            storedSession = null
        }
    }
}

