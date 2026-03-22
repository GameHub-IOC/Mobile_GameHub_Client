package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.LoginRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Define los endpoints de autenticación expuestos por el backend.
 *
 * Esta interfaz es utilizada por Retrofit para generar la implementación
 * concreta de las llamadas HTTP relacionadas con el inicio de sesión.
 */
interface AuthApi {

    /**
     * Envía una petición de login al servidor con las credenciales del usuario.
     *
     * La petición se realiza mediante `POST` al endpoint `auth/login`, enviando
     * en el cuerpo un objeto [LoginRequestDto]. Si la autenticación es correcta,
     * devuelve un [LoginResponseDto] con la información de sesión.
     *
     * @param request datos de autenticación que se enviarán al backend.
     * @return la respuesta del servidor con el token y los datos del usuario autenticado.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    /**
     * Envía una petición de registro al servidor con los datos del nuevo usuario.
     *
     * La petición se realiza mediante `POST` al endpoint `auth/register`, enviando
     * en el cuerpo un objeto [RegisterRequestDto]. Si el registro es correcto,
     * devuelve un [RegisterResponseDto] con la información de sesión.
     *
     * @param request datos del nuevo usuario que se enviarán al backend.
     * @return la respuesta del servidor
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto
}