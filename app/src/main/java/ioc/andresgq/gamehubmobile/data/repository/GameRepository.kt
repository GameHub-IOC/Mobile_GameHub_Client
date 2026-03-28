package ioc.andresgq.gamehubmobile.data.repository

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
 * Esta clase centraliza las llamadas a [GameApi] y expone métodos sencillos
 * para que la capa de presentación pueda consultar:
 * - todos los juegos,
 * - juegos por categoría,
 * - juegos disponibles,
 * - y opcionalmente el detalle de un juego.
 *
 * Por ahora el repositorio devuelve directamente [GameDto], ya que todavía
 * no existe un modelo de dominio específico para juegos.
 *
 * @property gameApi servicio remoto que expone las operaciones del catálogo.
 * @property ioDispatcher dispatcher usado para ejecutar operaciones de red.
 */
class GameRepository(
    private val gameApi: GameApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Recupera el catálogo completo de juegos.
     *
     * @return un [Result] con la lista de juegos en caso de éxito.
     */
    suspend fun getGames(): Result<List<GameDto>> {
        return executeRequest {
            gameApi.getGames()
        }
    }

    /**
     * Recupera los juegos pertenecientes a una categoría concreta.
     *
     * Si el nombre de categoría llega vacío o solo con espacios, se devuelve
     * un error controlado sin lanzar la petición al backend.
     *
     * @param categoryName nombre de la categoría por la que filtrar.
     * @return un [Result] con la lista filtrada.
     */
    suspend fun getGamesByCategory(categoryName: String): Result<List<GameDto>> {
        val normalizedCategory = categoryName.trim()

        if (normalizedCategory.isBlank()) {
            return Result.failure(Exception("El nombre de la categoría no puede estar vacío"))
        }

        return executeRequest {
            gameApi.getGamesByCategory(normalizedCategory)
        }
    }

    /**
     * Recupera únicamente los juegos disponibles.
     *
     * @return un [Result] con la lista de juegos disponibles.
     */
    suspend fun getAvailableGames(): Result<List<GameDto>> {
        return executeRequest {
            gameApi.getAvailableGames()
        }
    }

    /**
     * Ejecuta una petición remota que devuelve una lista de juegos,
     * aplicando un manejo homogéneo de errores.
     *
     * @param request bloque suspendido que realiza la llamada remota.
     * @return un [Result] con la lista obtenida o un error adaptado.
     */
    private suspend fun executeRequest(
        request: suspend () -> List<GameDto>
    ): Result<List<GameDto>> {
        return withContext(ioDispatcher) {
            try {
                Result.success(request())
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    404 -> "No se encontraron juegos"
                    else -> "Error del servidor (${e.code()})"
                }
                Result.failure(Exception(message))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(
                    Exception("Error inesperado al obtener los juegos: ${e.message}")
                )
            }
        }
    }
}