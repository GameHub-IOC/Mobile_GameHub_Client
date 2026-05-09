package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.OcupacionResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationAdminRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationListItemDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUserRequestDto
import ioc.andresgq.gamehubmobile.domain.reservation.CreateReservationCommand
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole

/**
 * Fuente de datos remota para operaciones de reservas.
 * Encapsula el acceso a Retrofit para desacoplar el repositorio de la API.
 *
 * @property reservationApi instancia de la API para reservas.
 */
class ReservationRemoteDataSource(
    private val reservationApi: ReservationApi
) {

    suspend fun getMyReservations(): List<ReservationListItemDto> =
        reservationApi.getMyReservations()

    suspend fun getAdminReservations(): List<ReservationListItemDto> =
        reservationApi.getAdminReservations()

    suspend fun getTurns(): List<ReservationTurnoDto> = reservationApi.getTurns()

    suspend fun getOperationalTables(): List<ReservationMesaOperativaDto> =
        reservationApi.getOperationalTables()

    suspend fun getFreeTables(fecha: String, turnoId: Long): List<ReservationMesaOperativaDto> =
        reservationApi.getFreeTables(fecha = fecha, turnoId = turnoId)

    /**
     * Devuelve el mapa de ocupación diario: estado de mesas y juegos por turno.
     *
     * @param fecha fecha a consultar en formato yyyy-MM-dd.
     */
    suspend fun getOcupacion(fecha: String): OcupacionResponseDto =
        reservationApi.getOcupacion(fecha = fecha)

    /**
     * Crea una reserva enviando AMBOS formatos de identificación (por ID y por nombre/número)
     * para que el servidor pueda usar el que prefiera sin lanzar NullPointerException.
     *
     * Formato primario del servidor  → mesaNumero + turnoNombre  (ejemplos 1/2 del Swagger)
     * Formato de compatibilidad      → mesaId     + turnoId      (ejemplo 3 del Swagger)
     */
    suspend fun createReservation(command: CreateReservationCommand) {
        val juegoNombre = command.juegoNombre?.trim().takeUnless { it.isNullOrBlank() }

        when (command.role) {
            UserRole.USER -> reservationApi.createReservationAsUser(
                ReservationUserRequestDto(
                    fecha = command.fecha,
                    mesaId = command.mesaId,
                    mesaNumero = command.mesaNumero,
                    turnoId = command.turnoId,
                    turnoNombre = command.turnoNombre,
                    juegoNombre = juegoNombre
                )
            )

            UserRole.ADMIN -> reservationApi.createReservationAsAdmin(
                ReservationAdminRequestDto(
                    fecha = command.fecha,
                    mesaId = command.mesaId,
                    mesaNumero = command.mesaNumero,
                    turnoId = command.turnoId,
                    turnoNombre = command.turnoNombre,
                    juegoNombre = juegoNombre,
                    usuarioNombre = command.usuarioNombre?.trim().takeUnless { it.isNullOrBlank() }
                )
            )
        }
    }

    /**
     * Cancela/elimina una reserva por su id.
     */
    suspend fun deleteReservation(id: Long) =
        reservationApi.deleteReservation(id)

    /**
     * Devuelve las reservas de un usuario concreto (solo ADMIN).
     */
    suspend fun getReservationsByUser(nombreUsuario: String): List<ReservationListItemDto> =
        reservationApi.getReservationsByUser(nombreUsuario)
}


