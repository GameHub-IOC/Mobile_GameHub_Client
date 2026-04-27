package ioc.andresgq.gamehubmobile.data.local

import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto

/**
 * Fuente de datos local para la caché de juegos.
 *
 * Utiliza el DAO [GameDao] para interactuar con la base de datos local.
 *
 * @property gameDao DAO de la base de datos local.
 */
class GameLocalDataSource(
    private val gameDao: GameDao
) {
    /**
     * Recupera todos los juegos almacenados en caché.
     *
     * @return lista de [GameDto], o lista vacía si no hay datos.
     */
    suspend fun getGames(): List<GameDto> = gameDao.getAll().map { it.toDto() }

    /**
     * Recupera los juegos de la categoría especificada.
     *
     * @param categoryName nombre exacto de la categoría por la que filtrar.
     * @return lista de [GameDto] de esa categoría
     */
    suspend fun getGamesByCategory(categoryName: String): List<GameDto> =
        gameDao.getByCategory(categoryName).map { it.toDto() }

    /**
     * Recupera únicamente los juegos marcados como disponibles.
     *
     * @return lista de [GameDto] disponibles, o lista vacía si no hay datos.
     */
    suspend fun getAvailableGames(): List<GameDto> = gameDao.getAvailable().map { it.toDto() }

    /**
     * Recupera un juego concreto por su identificador.
     *
     * Útil para la futura pantalla de detalle sin necesidad de red.
     *
     * @param id identificador único del juego.
     */
    suspend fun getGameById(id: Long): GameDto? = gameDao.getById(id)?.toDto()

    /**
     * Inserta o reemplaza una lista de juegos en la caché local.
     *
     * @param games lista de [GameDto] a persistir.
     */
    suspend fun upsertGames(games: List<GameDto>) {
        gameDao.upsertAll(games.map { it.toEntity() })
    }
}

