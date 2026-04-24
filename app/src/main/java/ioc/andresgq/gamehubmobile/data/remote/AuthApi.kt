package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.LoginRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Define los endpoints de autenticación expuestos por el backend.
 */
interface AuthApi {

    /**
     * Envía credenciales de login. Devuelve token JWT + datos del usuario.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    /**
     * Registra un nuevo usuario con rol USER por defecto.
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto

    /**
     * Notifica al servidor el cierre de sesión.
     * Al ser JWT (stateless) el servidor no invalida el token, pero es buena
     * práctica llamarlo para registro de auditoría.
     */
    @POST("auth/logout")
    suspend fun logout()
}
