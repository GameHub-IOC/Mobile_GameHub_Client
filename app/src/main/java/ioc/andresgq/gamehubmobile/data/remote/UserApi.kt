package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.UserCreateRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.UserDto
import ioc.andresgq.gamehubmobile.data.remote.dto.UserUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Define los endpoints de gestión de usuarios expuestos por el backend.
 *
 * Todas las operaciones requieren rol ADMIN (el token JWT se añade automáticamente
 * mediante el interceptor de autenticación configurado en el contenedor de DI).
 */
interface UserApi {

    /**
     * Obtiene el listado completo de usuarios registrados en el sistema.
     * Solo accesible para administradores.
     */
    @GET("usuarios")
    suspend fun getAll(): List<UserDto>

    /**
     * Actualiza los datos de un usuario existente.
     *
     * Si se cambia el rol, el backend lo persiste inmediatamente.
     * No se envía contraseña para evitar restablecerla accidentalmente.
     *
     * @param id   identificador del usuario a modificar.
     * @param user nuevos datos del usuario (id, nombre, nuevo rol).
     */
    @PUT("usuarios/{id}")
    suspend fun update(
        @Path("id") id: Long,
        @Body user: UserUpdateRequestDto
    ): UserDto

    /**
     * Elimina permanentemente un usuario por su identificador.
     *
     * @param id identificador del usuario a eliminar.
     */
    @DELETE("usuarios/{id}")
    suspend fun delete(@Path("id") id: Long)

    /**
     * Crea un nuevo usuario desde el panel de administración.
     *
     * El backend cifra la contraseña antes de persistirla.
     * Permite asignar cualquier rol, incluido ADMIN.
     *
     * @param user datos del nuevo usuario (nombre, contraseña en texto plano, rol).
     */
    @POST("usuarios")
    suspend fun create(@Body user: UserCreateRequestDto): UserDto
}

