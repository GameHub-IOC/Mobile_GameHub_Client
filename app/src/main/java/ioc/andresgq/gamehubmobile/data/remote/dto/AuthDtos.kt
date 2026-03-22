package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * DTO usado para enviar las credenciales de inicio de sesión al servidor.
 *
 * Esta clase representa el cuerpo de la petición de login y contiene los
 * datos mínimos que la API necesita para autenticar al usuario.
 *
 * @property nombre nombre de usuario enviado al backend.
 * @property password contraseña asociada al usuario.
 */
data class LoginRequestDto(
    val nombre: String,
    val password: String
)

/**
 * DTO que representa la respuesta del servidor tras un inicio de sesión correcto.
 *
 * Contiene la información devuelta por la API para identificar la sesión
 * autenticada y el contexto del usuario autenticado.
 *
 * @property token token de autenticación generado por el servidor.
 * @property nombre nombre del usuario autenticado.
 * @property rol rol o tipo de usuario devuelto por el backend.
 */
data class LoginResponseDto(
    val token: String,
    val nombre: String,
    val rol: String
)

/**
 * DTO usado para enviar las credenciales de registro al servidor.
 *
 * Esta clase representa el cuerpo de la petición de registro y contiene
 * los datos mínimos que la API necesita para crear un nuevo usuario.
 *
 * @property nombre nombre de usuario enviado al backend.
 * @property password contraseña asociada al usuario.
 * @property email email asociado al usuario
 */
data class RegisterRequestDto(
    val nombre: String,
    val password: String,
    val email: String
)

// La respuesta puede reutilizar LoginResponseDto si el servidor
// devuelve token + nombre + rol tras el registro, o ser Unit si
// el servidor solo confirma el alta y obliga a hacer login después

/**
 * DTO que representa la respuesta del servidor tras un registro correcto.
 *
 * Contiene la información devuelta por la API para identificar la sesión
 * autenticada y el contexto del usuario autenticado.
 *
 * @property token token de autenticación generado por el servidor.
 * @property nombre nombre del usuario autenticado.
 * @property rol rol o tipo de usuario devuelto por el backend.
 */
data class RegisterResponseDto(
    val token: String,
    val nombre: String,
    val rol: String
)