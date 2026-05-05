package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.local.GameLocalDataSource
import ioc.andresgq.gamehubmobile.data.remote.GameRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameRequestDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio encargado de gestionar el acceso al catálogo de juegos.
 *
 * Implementa una estrategia **network-first con fallback a caché**:
 * 1. Intenta obtener los datos de [gameRemoteDataSource].
 * 2. Si tiene éxito, actualiza la caché en [gameLocalDataSource] y devuelve los datos remotos.
 * 3. Si falla la red, devuelve los datos almacenados localmente (si existen).
 * 4. Si tampoco hay caché, devuelve un [Result.failure] con el error original.
 *
 * @property gameRemoteDataSource fuente remota del catálogo.
 * @property gameLocalDataSource  fuente local basada en Room para la caché.
 * @property ioDispatcher dispatcher usado para ejecutar operaciones de I/O.
 */
class GameRepository(
    private val gameRemoteDataSource: GameRemoteDataSource,
    private val gameLocalDataSource: GameLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Recupera el catálogo completo de juegos.
     * Fallback: todos los juegos almacenados en caché.
     *
     * @return lista de juegos devueltos por el servidor.
     */
    suspend fun getGames(): Result<List<GameDto>> =
        executeWithCache(
            request = { gameRemoteDataSource.getGames() },
            cacheFallback = { gameLocalDataSource.getGames() },
            cacheUpdate = { gameLocalDataSource.upsertGames(it) }
        )

    /**
     * Recupera los juegos de una categoría concreta.
     * Fallback: juegos de esa categoría almacenados en caché.
     *
     * @param categoryName nombre de la categoría por la que filtrar.
     * @return lista de juegos devueltos por el servidor.
     */
    suspend fun getGamesByCategory(categoryName: String): Result<List<GameDto>> {
        val normalized = categoryName.trim()
        if (normalized.isBlank()) {
            return Result.failure(Exception("El nombre de la categoría no puede estar vacío"))
        }
        return executeWithCache(
            request = { gameRemoteDataSource.getGamesByCategory(normalized) },
            cacheFallback = { gameLocalDataSource.getGamesByCategory(normalized) },
            cacheUpdate = { gameLocalDataSource.upsertGames(it) }
        )
    }

    /**
     * Recupera únicamente los juegos disponibles.
     * Fallback: juegos disponibles almacenados en caché.
     *
     * @return lista de juegos devueltos por el servidor.
     */
    suspend fun getAvailableGames(): Result<List<GameDto>> =
        executeWithCache(
            request = { gameRemoteDataSource.getAvailableGames() },
            cacheFallback = { gameLocalDataSource.getAvailableGames() },
            cacheUpdate = { gameLocalDataSource.upsertGames(it) }
        )

    /**
     * Recupera el detalle de un juego por su id.
     * Fallback: juego almacenado en caché local si existe.
     *
     * @param id identificador único del juego.
     * @return el juego con el identificador especificado.
     */
    suspend fun getGameById(id: Long): Result<GameDto> = withContext(ioDispatcher) {
        try {
            val remote = gameRemoteDataSource.getGameById(id)
            gameLocalDataSource.upsertGames(listOf(remote))
            Result.success(remote)
        } catch (e: HttpException) {
            val cached = gameLocalDataSource.getGameById(id)
            if (cached != null) {
                Result.success(cached)
            } else {
                val message = when (e.code()) {
                    404 -> "Juego no encontrado"
                    else -> "Error del servidor (${e.code()})"
                }
                Result.failure(Exception(message))
            }
        } catch (_: IOException) {
            val cached = gameLocalDataSource.getGameById(id)
            if (cached != null) Result.success(cached)
            else Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener el juego: ${e.message}"))
        }
    }

    /**
     * Ejecuta una petición remota aplicando la estrategia network-first con caché.
     *
     * En caso de error de red o del servidor, intenta servir datos desde [cacheFallback].
     * Solo propaga el error si la caché también está vacía.
     */
    private suspend fun executeWithCache(
        request: suspend () -> List<GameDto>,
        cacheFallback: suspend () -> List<GameDto>,
        cacheUpdate: suspend (List<GameDto>) -> Unit
    ): Result<List<GameDto>> = withContext(ioDispatcher) {
        try {
            val remote = request()
            cacheUpdate(remote)
            Result.success(remote)
        } catch (e: HttpException) {
            val cached = cacheFallback()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                val message = when (e.code()) {
                    404 -> "No se encontraron juegos"
                    else -> "Error del servidor (${e.code()})"
                }
                Result.failure(Exception(message))
            }
        } catch (_: IOException) {
            val cached = cacheFallback()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener los juegos: ${e.message}"))
        }
    }

    // ──────────────────────────────────────────────────
    //  Operaciones de escritura (requieren rol ADMIN)
    // ──────────────────────────────────────────────────

    /**
     * Crea un nuevo juego en el catálogo.
     *
     * @param request datos del nuevo juego.
     * @return el [GameDto] creado por el servidor.
     */
    suspend fun createGame(request: GameRequestDto): Result<GameDto> =
        withContext(ioDispatcher) {
            try {
                val created = gameRemoteDataSource.createGame(request)
                gameLocalDataSource.upsertGame(created)
                Result.success(created)
            } catch (e: HttpException) {
                Result.failure(Exception("Error del servidor (${e.code()})"))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al crear el juego: ${e.message}"))
            }
        }

    /**
     * Actualiza los datos de un juego existente.
     *
     * @param id      id del juego a modificar.
     * @param request nuevos datos.
     * @return el [GameDto] actualizado devuelto por el servidor.
     */
    suspend fun updateGame(id: Long, request: GameRequestDto): Result<GameDto> =
        withContext(ioDispatcher) {
            try {
                val updated = gameRemoteDataSource.updateGame(id, request)
                gameLocalDataSource.upsertGame(updated)
                Result.success(updated)
            } catch (e: HttpException) {
                Result.failure(Exception("Error del servidor (${e.code()})"))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al actualizar el juego: ${e.message}"))
            }
        }

    /**
     * Elimina un juego del catálogo y lo borra de la caché local.
     *
     * @param id id del juego a eliminar.
     */
    suspend fun deleteGame(id: Long): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                gameRemoteDataSource.deleteGame(id)
                gameLocalDataSource.deleteGame(id)
                Result.success(Unit)
            } catch (e: HttpException) {
                Result.failure(Exception("Error del servidor (${e.code()})"))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al eliminar el juego: ${e.message}"))
            }
        }
}