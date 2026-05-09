package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * DTO que representa un usuario del sistema tal como lo devuelve el backend.
 *
 * @property id       identificador único.
 * @property nombre   nombre de acceso único.
 * @property password contraseña cifrada (se ignora en UI; solo para deserialización).
 * @property rol      rol del usuario: "ADMIN" o "USER".
 */
data class UserDto(
    val id: Long = 0L,
    val nombre: String = "",
    val password: String? = null,
    val rol: String = "USER"
)

/**
 * DTO usado para actualizar el rol (u otros datos) de un usuario existente.
 *
 * No incluye [password] para evitar restablecerla accidentalmente.
 *
 * @property id     identificador del usuario a actualizar.
 * @property nombre nombre actual del usuario.
 * @property rol    nuevo rol a asignar: "ADMIN" o "USER".
 */
data class UserUpdateRequestDto(
    val id: Long,
    val nombre: String,
    val rol: String
)

/**
 * DTO usado para crear un nuevo usuario desde el panel de administración.
 *
 * El backend cifrará la contraseña automáticamente antes de persistirla.
 *
 * @property nombre   nombre de acceso único del nuevo usuario.
 * @property password contraseña en texto plano (el backend la cifra).
 * @property rol      rol inicial: "ADMIN" o "USER".
 */
data class UserCreateRequestDto(
    val nombre: String,
    val password: String,
    val rol: String
)

