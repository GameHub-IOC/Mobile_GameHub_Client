package ioc.andresgq.gamehubmobile.data.model

/**
 * Representa la sesión autenticada del usuario dentro de la aplicación.
 *
 * Este modelo agrupa la información mínima necesaria para identificar
 * una sesión activa y autorizar peticiones contra servicios protegidos.
 *
 * @property token token de autenticación asociado al usuario actual.
 * @property username nombre de usuario con el que se ha iniciado sesión.
 * @property userType tipo o rol del usuario dentro del sistema.
 */
data class UserSession(
    val token: String,
    val username: String,
    val userType: String
)