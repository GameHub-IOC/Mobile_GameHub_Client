package ioc.andresgq.gamehubmobile.data.local

import ioc.andresgq.gamehubmobile.data.model.UserSession

/**
 * Gestiona la persistencia local de la sesión del usuario.
 *
 * Esta clase actúa como una capa de abstracción sobre [UserSessionDao] para
 * guardar, recuperar y eliminar la información de sesión en la base de datos local.
 *
 * @property userSessionDao DAO encargado de acceder a la tabla de sesión.
 */
class TokenManager(
    private val userSessionDao: UserSessionDao
) {
    /**
     * Guarda o actualiza la sesión actual del usuario en almacenamiento local.
     *
     * Convierte el modelo de dominio [UserSession] en una entidad persistible
     * [UserSessionEntity] antes de delegar la operación al DAO.
     *
     * @param session información de sesión que se desea almacenar.
     */
    suspend fun saveSession(session: UserSession) {
        userSessionDao.upsert(
            UserSessionEntity(
                token = session.token,
                username = session.username,
                userType = session.userType
            )
        )
    }

    /**
     * Recupera la sesión almacenada localmente, si existe.
     *
     * Obtiene la entidad persistida desde el DAO y la transforma de nuevo al
     * modelo de dominio [UserSession]. Si no hay sesión guardada, devuelve `null`.
     *
     * @return la sesión del usuario almacenada o `null` si no existe.
     */
    suspend fun getSession(): UserSession? {
        return userSessionDao.getSession()?.let {
            UserSession(
                token = it.token,
                username = it.username,
                userType = it.userType
            )
        }
    }

    /**
     * Elimina la sesión almacenada localmente.
     *
     * Esta operación borra cualquier información persistida relacionada con la
     * sesión del usuario actual.
     */
    suspend fun clearSession() {
        userSessionDao.clear()
    }
}