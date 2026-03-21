package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la sesión persistida del usuario.
 *
 * Esta clase se almacena en la tabla `user_session` y contiene la información
 * mínima necesaria para mantener una sesión iniciada localmente, como el token
 * de autenticación, el nombre de usuario y el tipo de usuario.
 *
 * Se usa un único registro con `id = 0` por defecto, lo que indica que la app
 * está diseñada para guardar solo una sesión activa a la vez.
 *
 * @property id identificador único del registro de sesión. Su valor por defecto
 * es `0` para reutilizar siempre la misma fila.
 * @property token token de autenticación asociado a la sesión del usuario.
 * @property username nombre del usuario autenticado.
 * @property userType tipo o rol del usuario dentro de la aplicación.
 */
@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 0,
    val token: String,
    val username: String,
    val userType: String
)