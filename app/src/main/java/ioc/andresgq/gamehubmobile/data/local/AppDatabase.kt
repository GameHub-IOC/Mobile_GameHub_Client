package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos local de la aplicación gestionada por Room.
 *
 * Esta clase define las entidades incluidas en la base de datos y expone
 * los DAOs necesarios para acceder a ellas. En este caso, la base de datos
 * solo contiene la entidad [UserSessionEntity], usada para persistir la
 * información de sesión del usuario.
 */
@Database(
    entities = [UserSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al DAO de sesiones de usuario.
     *
     * @return una instancia de [UserSessionDao] para consultar y modificar
     * la información de sesión almacenada localmente.
     */
    abstract fun userSessionDao(): UserSessionDao
}