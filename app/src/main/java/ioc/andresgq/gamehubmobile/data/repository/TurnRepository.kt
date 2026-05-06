package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.remote.TurnApi
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio para la gestión administrativa de turnos horarios del local.
 *
 * @property turnApi API Retrofit para el endpoint de turnos.
 * @property ioDispatcher dispatcher de corrutinas para operaciones de I/O.
 */
class TurnRepository(
    private val turnApi: TurnApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /** Recupera el listado completo de turnos desde el servidor. */
    suspend fun getTurnos(): Result<List<ReservationTurnoDto>> = withContext(ioDispatcher) {
        try {
            Result.success(turnApi.getTurnos())
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener turnos: ${e.message}"))
        }
    }

    /**
     * Crea un nuevo turno.
     *
     * @param nombre     nombre identificativo del turno (p.e. "MAÑANA 1").
     * @param horaInicio hora de inicio en formato "HH:mm" o null.
     * @param horaFin    hora de fin en formato "HH:mm" o null.
     */
    suspend fun createTurno(
        nombre: String,
        horaInicio: String?,
        horaFin: String?
    ): Result<ReservationTurnoDto> = withContext(ioDispatcher) {
        try {
            Result.success(
                turnApi.crearTurno(
                    ReservationTurnoDto(
                        id = 0L,
                        nombre = nombre,
                        horaInicio = horaInicio,
                        horaFin = horaFin
                    )
                )
            )
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400  -> "Ya existe un turno con ese nombre"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(msg))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al crear el turno: ${e.message}"))
        }
    }

    /**
     * Actualiza un turno existente. El id se incluye en el propio objeto.
     *
     * @param id         id del turno a modificar.
     * @param nombre     nuevo nombre del turno.
     * @param horaInicio nueva hora de inicio en formato "HH:mm" o null.
     * @param horaFin    nueva hora de fin en formato "HH:mm" o null.
     */
    suspend fun updateTurno(
        id: Long,
        nombre: String,
        horaInicio: String?,
        horaFin: String?
    ): Result<ReservationTurnoDto> = withContext(ioDispatcher) {
        try {
            Result.success(
                turnApi.actualizarTurno(
                    ReservationTurnoDto(
                        id = id,
                        nombre = nombre,
                        horaInicio = horaInicio,
                        horaFin = horaFin
                    )
                )
            )
        } catch (e: HttpException) {
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al actualizar el turno: ${e.message}"))
        }
    }

    /** Elimina un turno por su id. Falla si tiene reservas activas. */
    suspend fun deleteTurno(id: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            turnApi.eliminarTurnoPorId(id)
            Result.success(Unit)
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400  -> "No se puede eliminar: el turno tiene reservas activas asociadas"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(msg))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al eliminar el turno: ${e.message}"))
        }
    }
}

