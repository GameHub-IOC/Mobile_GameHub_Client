package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.remote.UserApi
import ioc.andresgq.gamehubmobile.data.remote.dto.UserCreateRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.UserDto
import ioc.andresgq.gamehubmobile.data.remote.dto.UserUpdateRequestDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio para la gestión administrativa de usuarios.
 *
 * Expone operaciones de lectura (listar), modificación de rol y eliminación.
 * Todas las operaciones de escritura requieren rol ADMIN en el backend.
 *
 * @property userApi  servicio Retrofit que mapea los endpoints `/usuarios`.
 * @property ioDispatcher dispatcher de corrutinas para operaciones de I/O.
 */
class UserRepository(
    private val userApi: UserApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Recupera la lista completa de usuarios registrados en el sistema.
     *
     * @return [Result] con la lista de [UserDto] en caso de éxito, o un error encapsulado.
     */
    suspend fun getUsers(): Result<List<UserDto>> = withContext(ioDispatcher) {
        try {
            Result.success(userApi.getAll())
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                403  -> "Acceso denegado: se requiere rol ADMIN"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(msg))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener usuarios: ${e.message}"))
        }
    }

    /**
     * Cambia el rol de un usuario existente.
     *
     * Envía al backend solo el id, nombre y nuevo rol para no alterar la contraseña.
     *
     * @param id      identificador del usuario.
     * @param nombre  nombre actual del usuario (requerido por el backend).
     * @param newRole nuevo rol a asignar: `"ADMIN"` o `"USER"`.
     * @return [Result.success] con [Unit] si la operación se completó correctamente.
     */
    suspend fun changeRole(id: Long, nombre: String, newRole: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                userApi.update(id, UserUpdateRequestDto(id = id, nombre = nombre, rol = newRole))
                Result.success(Unit)
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    403  -> "Sin permisos para modificar este usuario"
                    404  -> "Usuario no encontrado"
                    else -> "Error del servidor (${e.code()})"
                }
                Result.failure(Exception(msg))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al actualizar el rol: ${e.message}"))
            }
        }

    /**
     * Elimina permanentemente un usuario por su identificador.
     *
     * @param id identificador del usuario a eliminar.
     * @return [Result.success] con [Unit] si se eliminó correctamente.
     */
    suspend fun deleteUser(id: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            userApi.delete(id)
            Result.success(Unit)
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                403  -> "Sin permisos para eliminar usuarios"
                404  -> "Usuario no encontrado"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(msg))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al eliminar el usuario: ${e.message}"))
        }
    }

    /**
     * Crea un nuevo usuario desde el panel de administración.
     *
     * Permite asignar directamente el rol (incluido ADMIN), a diferencia del
     * registro público que siempre asigna USER.
     *
     * @param nombre   nombre de acceso único del nuevo usuario.
     * @param password contraseña en texto plano (el backend la cifra).
     * @param rol      rol inicial: `"ADMIN"` o `"USER"`.
     * @return [Result.success] con el [UserDto] creado, o error encapsulado.
     */
    suspend fun createUser(nombre: String, password: String, rol: String): Result<UserDto> =
        withContext(ioDispatcher) {
            try {
                val created = userApi.create(UserCreateRequestDto(nombre = nombre, password = password, rol = rol))
                Result.success(created)
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    400  -> "El nombre de usuario \"$nombre\" ya existe"
                    403  -> "Sin permisos para crear usuarios"
                    else -> "Error del servidor (${e.code()})"
                }
                Result.failure(Exception(msg))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al crear el usuario: ${e.message}"))
            }
        }
}

