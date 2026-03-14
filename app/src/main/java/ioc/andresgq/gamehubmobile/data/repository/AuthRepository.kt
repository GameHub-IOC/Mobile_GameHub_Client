package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.local.TokenManager
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.data.remote.AuthApi
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(username: String, password: String): Result<UserSession> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.login(
                    LoginRequestDto(
                        nombre = username,
                        password = password
                    )
                )
                val session = UserSession(
                    token = response.token,
                    username = response.nombre,
                    userType = response.rol
                )
                tokenManager.saveSession(session)
                Result.success(session)
            } catch (httpException: HttpException) {
                Result.failure(Exception("Credenciales inválidas o error ${httpException.code()}"))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (_: Exception) {
                Result.failure(Exception("Error inesperado durante login"))
            }
        }
    }

    suspend fun getSession(): UserSession? {
        return withContext(Dispatchers.IO) {
            tokenManager.getSession()
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            tokenManager.clearSession()
        }
    }
}
