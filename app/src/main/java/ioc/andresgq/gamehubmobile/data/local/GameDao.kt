package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO de Room para acceder y modificar la caché local del catálogo de juegos.
 *
 * Expone consultas optimizadas para los tres casos de uso del [GameRepository]:
 * catálogo completo, filtrado por categoría y filtrado por disponibilidad.
 * El uso de [OnConflictStrategy.REPLACE] en [upsertAll] garantiza que los datos
 * remotos siempre sobreescriban los registros existentes sin generar duplicados.
 */
@Dao
interface GameDao {

    /**
     * Recupera todos los juegos almacenados en caché, ordenados por nombre.
     *
     * @return lista completa de juegos en caché, o lista vacía si no hay datos.
     */
    @Query("SELECT * FROM games ORDER BY nombre ASC")
    suspend fun getAll(): List<GameEntity>

    /**
     * Recupera los juegos que pertenecen a la categoría indicada.
     *
     * @param categoryName nombre exacto de la categoría por la que filtrar.
     * @return lista de juegos de esa categoría, o lista vacía si no hay coincidencias.
     */
    @Query("SELECT * FROM games WHERE categoriaNombre = :categoryName ORDER BY nombre ASC")
    suspend fun getByCategory(categoryName: String): List<GameEntity>

    /**
     * Recupera únicamente los juegos marcados como disponibles.
     *
     * @return lista de juegos disponibles, o lista vacía si no hay ninguno.
     */
    @Query("SELECT * FROM games WHERE disponible = 1 ORDER BY nombre ASC")
    suspend fun getAvailable(): List<GameEntity>

    /**
     * Recupera un juego concreto por su identificador.
     *
     * Útil para la futura pantalla de detalle sin necesidad de red.
     *
     * @param id identificador único del juego.
     * @return el [GameEntity] encontrado, o `null` si no está en caché.
     */
    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Long): GameEntity?

    /**
     * Inserta o reemplaza una lista de juegos en la caché local.
     *
     * Usa [OnConflictStrategy.REPLACE] para que las actualizaciones remotas
     * sobreescriban los registros existentes sin duplicar entradas.
     *
     * @param games lista de entidades a persistir.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(games: List<GameEntity>)

    /**
     * Inserta o reemplaza un único juego en la caché local.
     *
     * @param game entidad a persistir.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOne(game: GameEntity)

    /**
     * Elimina todos los juegos almacenados en caché.
     *
     * Útil para forzar una recarga completa desde la red.
     */
    @Query("DELETE FROM games")
    suspend fun clearAll()

    /**
     * Elimina un juego concreto de la caché por su id.
     *
     * @param id identificador único del juego a eliminar.
     */
    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Long)
}