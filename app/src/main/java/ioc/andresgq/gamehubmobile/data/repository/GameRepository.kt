package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.local.GameDao
import ioc.andresgq.gamehubmobile.data.local.GameEntity
import ioc.andresgq.gamehubmobile.data.local.toDto
import ioc.andresgq.gamehubmobile.data.local.toEntity
import ioc.andresgq.gamehubmobile.data.remote.GameApi
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
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
 * 1. Intenta obtener los datos de [gameApi].
 * 2. Si tiene éxito, actualiza la caché en [gameDao] y devuelve los datos remotos.
 * 3. Si falla la red, devuelve los datos almacenados localmente (si existen).
 * 4. Si tampoco hay caché, devuelve un [Result.failure] con el error original.
 *
 * @property gameApi   servicio remoto que expone las operaciones del catálogo.
 * @property gameDao   DAO de Room para lectura y escritura de la caché local.
 * @property ioDispatcher dispatcher usado para ejecutar operaciones de I/O.
 */
class GameRepository(
    private val gameApi: GameApi,
    private val gameDao: GameDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Recupera el catálogo completo de juegos.
     * Fallback: todos los juegos almacenados en caché.
     */
    suspend fun getGames(): Result<List<GameDto>> =
        executeWithCache(
            request = { gameApi.getGames() },
            cacheFallback = { gameDao.getAll() },
            cacheUpdate = { gameDao.upsertAll(it.map { dto -> dto.toEntity() }) }
        )

    /**
     * Recupera los juegos de una categoría concreta.
     * Fallback: juegos de esa categoría almacenados en caché.
     *
     * @param categoryName nombre de la categoría por la que filtrar.
     */
    suspend fun getGamesByCategory(categoryName: String): Result<List<GameDto>> {
        val normalized = categoryName.trim()
        if (normalized.isBlank()) {
            return Result.failure(Exception("El nombre de la categoría no puede estar vacío"))
        }
        return executeWithCache(
            request = { gameApi.getGamesByCategory(normalized) },
            cacheFallback = { gameDao.getByCategory(normalized) },
            cacheUpdate = { gameDao.upsertAll(it.map { dto -> dto.toEntity() }) }
        )
    }

    /**
     * Recupera únicamente los juegos disponibles.
     * Fallback: juegos disponibles almacenados en caché.
     */
    suspend fun getAvailableGames(): Result<List<GameDto>> =
        executeWithCache(
            request = { gameApi.getAvailableGames() },
            cacheFallback = { gameDao.getAvailable() },
            cacheUpdate = { gameDao.upsertAll(it.map { dto -> dto.toEntity() }) }
        )

    /**
     * Ejecuta una petición remota aplicando la estrategia network-first con caché.
     *
     * En caso de error de red o del servidor, intenta servir datos desde [cacheFallback].
     * Solo propaga el error si la caché también está vacía.
     *
     * @param request       llamada suspendida a la API remota.
     * @param cacheFallback consulta local como respaldo ante fallos de red.
     * @param cacheUpdate   acción para persistir los datos remotos en Room.
     */
    private suspend fun executeWithCache(
        request: suspend () -> List<GameDto>,
        cacheFallback: suspend () -> List<GameEntity>,
        cacheUpdate: suspend (List<GameDto>) -> Unit
    ): Result<List<GameDto>> = withContext(ioDispatcher) {
        try {
            val remote = request()
            cacheUpdate(remote)
            Result.success(remote)
        } catch (e: HttpException) {
            val cached = cacheFallback()
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toDto() })
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
                Result.success(cached.map { it.toDto() })
            } else {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener los juegos: ${e.message}"))
        }
    }
}