package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.remote.ReservationRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.dto.OcupacionResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationListItemDto
import ioc.andresgq.gamehubmobile.domain.reservation.CreateReservationCommand
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationTableOption
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationTurnOption
import ioc.andresgq.gamehubmobile.ui.screens.reservations.ReservationListItemUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio encargado de crear reservas contra el backend.
 */
class ReservationRepository(
    private val remoteDataSource: ReservationRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getMyReservations(): Result<List<ReservationListItemUi>> = withContext(ioDispatcher) {
        try {
            val reservations = remoteDataSource.getMyReservations()
                .map { it.toUi() }
                .sortedByDescending { it.fecha }
            Result.success(reservations)
        } catch (e: HttpException) {
            Result.failure(Exception(httpErrorMessage(e.code(), "reservas")))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener tus reservas: ${e.message}"))
        }
    }

    suspend fun getAdminReservations(): Result<List<ReservationListItemUi>> = withContext(ioDispatcher) {
        try {
            val reservations = remoteDataSource.getAdminReservations()
                .map { it.toUi() }
                .sortedByDescending { it.fecha }
            Result.success(reservations)
        } catch (e: HttpException) {
            Result.failure(Exception(httpErrorMessage(e.code(), "reservas")))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener reservas: ${e.message}"))
        }
    }

    /**
     * Obtiene los turnos disponibles incluyendo franja horaria formateada.
     */
    suspend fun getTurnOptions(): Result<List<ReservationTurnOption>> = withContext(ioDispatcher) {
        try {
            val turns = remoteDataSource.getTurns()
                .map { dto ->
                    ReservationTurnOption(
                        id = dto.id,
                        nombre = dto.nombre,
                        horaInicio = dto.horaInicio?.take(5),   // "HH:mm:ss" → "HH:mm"
                        horaFin = dto.horaFin?.take(5)
                    )
                }
                .sortedBy { it.id }
            Result.success(turns)
        } catch (e: HttpException) {
            Result.failure(Exception(httpErrorMessage(e.code(), "turnos")))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener turnos: ${e.message}"))
        }
    }

    /**
     * Obtiene todas las mesas operativas (id, numero, capacidad).
     */
    suspend fun getOperationalTableOptions(): Result<List<ReservationTableOption>> = withContext(ioDispatcher) {
        try {
            val tables = remoteDataSource.getOperationalTables()
                .map { ReservationTableOption(id = it.id, numero = it.numero, capacidad = it.capacidad) }
                .sortedBy { it.numero }
            Result.success(tables)
        } catch (e: HttpException) {
            Result.failure(Exception(httpErrorMessage(e.code(), "mesas")))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener mesas: ${e.message}"))
        }
    }

    /**
     * Obtiene las mesas libres para una fecha y turno concretos.
     */
    suspend fun getFreeTableOptions(fecha: String, turnoId: Long): Result<List<ReservationTableOption>> = withContext(ioDispatcher) {
        if (!DATE_REGEX.matches(fecha.trim())) {
            return@withContext Result.failure(Exception("La fecha debe tener formato $DATE_FORMAT_LABEL"))
        }
        if (turnoId <= 0L) {
            return@withContext Result.failure(Exception("Selecciona un turno válido"))
        }

        try {
            val tables = remoteDataSource.getFreeTables(fecha = fecha.trim(), turnoId = turnoId)
                .map { ReservationTableOption(id = it.id, numero = it.numero, capacidad = it.capacidad) }
                .sortedBy { it.numero }
            Result.success(tables)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                400 -> "Fecha o turno no válidos para consultar mesas libres"
                401 -> "Tu sesión ha caducado"
                403 -> "No tienes permisos para consultar mesas libres"
                404 -> "No se encontraron mesas libres"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(message))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener mesas libres: ${e.message}"))
        }
    }

    /**
     * Devuelve el mapa de ocupación diario de mesas y juegos por turno.
     * Útil para el mapa visual de mesas en el wizard.
     *
     * @param fecha fecha a consultar en formato yyyy-MM-dd.
     */
    suspend fun getOcupacion(fecha: String): Result<OcupacionResponseDto> = withContext(ioDispatcher) {
        if (!DATE_REGEX.matches(fecha.trim())) {
            return@withContext Result.failure(Exception("La fecha debe tener formato $DATE_FORMAT_LABEL"))
        }
        try {
            val response = remoteDataSource.getOcupacion(fecha = fecha.trim())
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(Exception(httpErrorMessage(e.code(), "ocupación")))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado al obtener la ocupación: ${e.message}"))
        }
    }

    suspend fun createReservation(command: CreateReservationCommand): Result<Unit> {
        val validationError = validate(command)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        return withContext(ioDispatcher) {
            try {
                remoteDataSource.createReservation(command)
                Result.success(Unit)
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "Datos de reserva no válidos"
                    401 -> "Tu sesión ha caducado"
                    403 -> "No tienes permisos para crear esta reserva"
                    409 -> "La mesa o el turno ya no están disponibles"
                    else -> "Error del servidor (${e.code()})"
                }
                Result.failure(Exception(message))
            } catch (_: IOException) {
                Result.failure(Exception("No se pudo conectar con el servidor"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado al crear la reserva: ${e.message}"))
            }
        }
    }

    private fun validate(command: CreateReservationCommand): String? {
        if (!DATE_REGEX.matches(command.fecha.trim())) {
            return "La fecha debe tener formato ${DATE_FORMAT_LABEL}"
        }
        if (command.turnoId <= 0L) {
            return "El turno seleccionado no es válido"
        }
        if (command.mesaId <= 0L) {
            return "La mesa seleccionada no es válida"
        }
        if (command.role == UserRole.ADMIN && command.usuarioNombre.isNullOrBlank()) {
            return "El administrador debe indicar el usuario de la reserva"
        }
        return null
    }

    private fun ReservationListItemDto.toUi(): ReservationListItemUi {
        return ReservationListItemUi(
            id = id,
            fecha = fecha,
            estado = estado.orEmpty().ifBlank { "PENDIENTE" },
            mesaNumero = mesa?.numero,
            turnoNombre = turno?.nombre.orEmpty().ifBlank { "Turno" },
            juegoNombre = juego?.nombre.orEmpty().ifBlank { "Sin juego" },
            usuarioNombre = usuario?.nombre.orEmpty().ifBlank { "-" }
        )
    }

    private companion object {
        const val DATE_FORMAT_LABEL = "yyyy-MM-dd"
        val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

        fun httpErrorMessage(code: Int, resource: String): String {
            return when (code) {
                401 -> "Tu sesión ha caducado"
                403 -> "No tienes permisos para consultar $resource"
                404 -> "No se encontraron $resource disponibles"
                else -> "Error del servidor ($code)"
            }
        }
    }
}
