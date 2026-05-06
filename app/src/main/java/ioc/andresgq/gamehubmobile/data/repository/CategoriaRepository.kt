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
 * @property categoriaApi API Retrofit para el endpoint de categorías.
 * @property ioDispatcher dispatcher de corrutinas para operaciones de I/O.
 */
class CategoriaRepository(
    private val categoriaApi: CategoriaApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /** Recupera el listado completo de categorías desde el servidor. */
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

    /** Crea una nueva categoría en el servidor. */
    suspend fun createCategoria(nombre: String): Result<CategoriaDto> = withContext(ioDispatcher) {
        try {
            Result.success(categoriaApi.crearCategoria(CategoriaDto(nombre = nombre)))
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al crear la categoría: ${e.message}"))
        }
    }

    /** Actualiza el nombre de una categoría existente (identificada por [id]). */
    suspend fun updateCategoria(id: Long, nuevoNombre: String): Result<CategoriaDto> =
        withContext(ioDispatcher) {
            try {
                Result.success(
                    categoriaApi.actualizarCategoriaPorId(id, CategoriaDto(id = id, nombre = nuevoNombre))
                )
            } catch (e: HttpException) {
                Result.failure(Exception("Error del servidor (${e.code()})"))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al actualizar la categoría: ${e.message}"))
            }
        }

    /** Elimina una categoría por su nombre. */
    suspend fun deleteCategoria(nombre: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            categoriaApi.eliminarCategoriaPorNombre(nombre)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al eliminar la categoría: ${e.message}"))
        }
    }
}

