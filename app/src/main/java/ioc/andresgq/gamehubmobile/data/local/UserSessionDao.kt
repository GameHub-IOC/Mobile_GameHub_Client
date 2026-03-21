package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de Room para acceder y modificar la sesión persistida del usuario.
 *
 * Esta interfaz encapsula las operaciones básicas sobre la tabla `user_session`,
 * permitiendo recuperar la sesión actual, guardarla o reemplazarla, y eliminarla.
 */
@Dao
interface UserSessionDao {

    /**
     * Recupera la sesión almacenada actualmente.
     *
     * La consulta busca el registro con `id = 0`, lo que sugiere que la tabla
     * está diseñada para mantener una única sesión activa.
     *
     * @return la entidad [UserSessionEntity] almacenada, o `null` si no existe sesión.
     */
    @Query("SELECT * FROM user_session WHERE id = 0 LIMIT 1")
    suspend fun getSession(): UserSessionEntity?

    /**
     * Inserta o reemplaza la sesión actual en la base de datos.
     *
     * Usa [OnConflictStrategy.REPLACE] para sobrescribir el registro existente
     * cuando haya conflicto de clave primaria o única.
     *
     * @param session entidad de sesión que se desea guardar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: UserSessionEntity)

    /**
     * Elimina todas las sesiones almacenadas en la tabla `user_session`.
     *
     * Resulta útil para cerrar sesión o limpiar el estado local del usuario.
     */
    @Query("DELETE FROM user_session")
    suspend fun clear()
}