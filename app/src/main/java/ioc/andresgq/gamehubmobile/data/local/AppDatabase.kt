package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ioc.andresgq.gamehubmobile.data.local.AppDatabase.Companion.MIGRATION_1_2

/**
 * Base de datos local de la aplicación gestionada por Room.
 *
 * Incluye las entidades [UserSessionEntity] (sesión del usuario) y [GameEntity]
 * (caché del catálogo de juegos). La versión 2 añade la tabla `games`.
 *
 * La migración [MIGRATION_1_2] crea la nueva tabla sin destruir los datos de
 * sesión existentes, garantizando compatibilidad con instalaciones previas.
 */
@Database(
    entities = [UserSessionEntity::class, GameEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Acceso al DAO de sesión de usuario. */
    abstract fun userSessionDao(): UserSessionDao

    /** Acceso al DAO de caché de juegos. */
    abstract fun gameDao(): GameDao

    companion object {
        /**
         * Migración de la versión 1 a la 2.
         *
         * Crea la tabla `games` que almacena el catálogo en caché local.
         * No modifica ninguna tabla existente, por lo que la sesión del usuario
         * se conserva intacta tras la actualización.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `games` (
                        `id`              INTEGER NOT NULL,
                        `nombre`          TEXT    NOT NULL,
                        `numJugadores`    TEXT    NOT NULL,
                        `categoriaId`     INTEGER NOT NULL,
                        `categoriaNombre` TEXT    NOT NULL,
                        `disponible`      INTEGER NOT NULL,
                        `descripcion`     TEXT,
                        `rutaImagen`      TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}