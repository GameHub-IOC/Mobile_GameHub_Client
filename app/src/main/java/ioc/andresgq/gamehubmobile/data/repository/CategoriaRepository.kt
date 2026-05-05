package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.remote.CategoriaApi
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoriaDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio para la gestión de categorías de juegos.
 *
 * En esta primera fase expone solo la lectura del catálogo de categorías,
 * necesaria para la pantalla de gestión administrativa.
 *
 * @property categoriaApi API Retrofit para el endpoint de categorías.
 * @property ioDispatcher dispatcher de corrutinas para operaciones de I/O.
 */
class CategoriaRepository(
    private val categoriaApi: CategoriaApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Recupera el listado completo de categorías desde el servidor.
     *
     * @return [Result] con la lista de [CategoriaDto] o un error descriptivo.
     */
    suspend fun getCategorias(): Result<List<CategoriaDto>> = withContext(ioDispatcher) {
        try {
            Result.success(categoriaApi.getCategorias())
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener categorías: ${e.message}"))
        }
    }
}

