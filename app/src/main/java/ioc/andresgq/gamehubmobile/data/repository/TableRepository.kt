package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.remote.TableApi
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio para la gestión administrativa de mesas del local.
 *
 * @property tableApi API Retrofit para el endpoint de mesas.
 * @property ioDispatcher dispatcher de corrutinas para operaciones de I/O.
 */
class TableRepository(
    private val tableApi: TableApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /** Recupera el listado completo de mesas desde el servidor. */
    suspend fun getMesas(): Result<List<ReservationMesaOperativaDto>> = withContext(ioDispatcher) {
        try {
            Result.success(tableApi.getMesas())
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener mesas: ${e.message}"))
        }
    }

    /** Crea una nueva mesa en el local. */
    suspend fun createMesa(numero: Int, capacidad: Int): Result<ReservationMesaOperativaDto> =
        withContext(ioDispatcher) {
            try {
                Result.success(
                    tableApi.crearMesa(
                        ReservationMesaOperativaDto(numero = numero, capacidad = capacidad, operativa = true)
                    )
                )
            } catch (e: HttpException) {
                Result.failure(Exception("Error del servidor (${e.code()})"))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al crear la mesa: ${e.message}"))
            }
        }

    /**
     * Actualiza los datos de una mesa existente.
     *
     * @param id        id de la mesa.
     * @param numero    nuevo número visible de la mesa.
     * @param capacidad nueva capacidad máxima.
     * @param operativa nuevo estado (operativa / fuera de servicio).
     */
    suspend fun updateMesa(
        id: Long,
        numero: Int,
        capacidad: Int,
        operativa: Boolean
    ): Result<ReservationMesaOperativaDto> = withContext(ioDispatcher) {
        try {
            Result.success(
                tableApi.actualizarMesaPorId(
                    id,
                    ReservationMesaOperativaDto(id = id, numero = numero, capacidad = capacidad, operativa = operativa)
                )
            )
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al actualizar la mesa: ${e.message}"))
        }
    }

    /** Elimina una mesa por su id. Falla si tiene reservas asociadas. */
    suspend fun deleteMesa(id: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            tableApi.eliminarMesaPorId(id)
            Result.success(Unit)
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400  -> "No se puede eliminar: la mesa tiene reservas asociadas"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(msg))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al eliminar la mesa: ${e.message}"))
        }
    }
}

