package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.local.TokenManager
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.data.remote.AuthApi
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterRequestDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio encargado de gestionar la autenticación del usuario.
 *
 * Esta clase coordina el acceso a la API remota de autenticación mediante [AuthApi]
 * y la persistencia local de la sesión a través de [TokenManager].
 *
 * Sus responsabilidades principales son:
 * - enviar las credenciales al servidor,
 * - transformar la respuesta remota en un modelo de dominio [UserSession],
 * - guardar la sesión localmente,
 * - recuperar la sesión guardada,
 * - y eliminarla al cerrar sesión.
 *
 * @property authApi servicio remoto que expone las operaciones de autenticación.
 * @property tokenManager componente responsable de persistir la sesión localmente.
 */
class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     *
     * La operación se ejecuta en el dispatcher de entrada/salida ([Dispatchers.IO]),
     * ya que implica una llamada de red y una escritura en almacenamiento local.
     *
     * Flujo del método:
     * 1. Construye un [LoginRequestDto] con el nombre de usuario y la contraseña.
     * 2. Llama al endpoint remoto de login usando [authApi].
     * 3. Convierte la respuesta en un objeto de dominio [UserSession].
     * 4. Guarda la sesión localmente mediante [tokenManager].
     * 5. Devuelve el resultado encapsulado en [Result].
     *
     * También captura errores frecuentes para devolver mensajes más comprensibles:
     * - [HttpException] para errores HTTP del servidor.
     * - [IOException] para problemas de conectividad.
     * - [Exception] para cualquier otro error inesperado.
     *
     * @param username nombre de usuario introducido por el usuario.
     * @param password contraseña introducida por el usuario.
     * @return un [Result] con la [UserSession] en caso de éxito, o un error en caso de fallo.
     */
    suspend fun login(username: String, password: String): Result<UserSession> {
        return withContext(ioDispatcher) {
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
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Result.failure(Exception("Error inesperado durante login"))
            }
        }
    }

    /**
     * Recupera la sesión actualmente almacenada en local, si existe.
     *
     * La lectura se ejecuta sobre [Dispatchers.IO] porque accede a almacenamiento
     * persistente a través de [TokenManager].
     *
     * @return la [UserSession] guardada localmente, o `null` si no hay sesión activa.
     */
    suspend fun getSession(): UserSession? {
        return withContext(ioDispatcher) {
            tokenManager.getSession()
        }
    }

    /**
     * Cierra la sesión del usuario.
     *
     * Notifica al servidor (para auditoría) y luego borra la sesión local.
     * Si la llamada al servidor falla, se ignora el error y se procede a limpiar localmente.
     */
    suspend fun logout() {
        withContext(ioDispatcher) {
            try {
                authApi.logout()
            } catch (_: Exception) {
                // JWT es stateless: aunque la llamada falle, limpiamos localmente
            }
            tokenManager.clearSession()
        }
    }

    suspend fun register(
        username: String,
        password: String
    ): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                authApi.register(
                    RegisterRequestDto(
                        nombre = username,
                        password = password
                    )
                )
                Result.success(Unit)
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    409  -> "Este nombre de usuario ya existe"
                    400  -> "Datos de registro incorrectos"
                    else -> "Error del servidor (${e.code()})"
                }
                Result.failure(Exception(message))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(
                    Exception("Error inesperado durante el registro: ${e.message}")
                )
            }
        }
    }
}