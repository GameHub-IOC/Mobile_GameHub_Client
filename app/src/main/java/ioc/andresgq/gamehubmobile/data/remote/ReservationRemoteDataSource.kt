package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.OcupacionResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationAdminRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationJuegoRefDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationListItemDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaIdRefDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoRefDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUserRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUsuarioRefDto
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
    private fun CreateReservationCommand.toJuegoRefOrNull(): ReservationJuegoRefDto? {
        val normalizedName = juegoNombre?.trim().takeUnless { it.isNullOrBlank() }
        if (normalizedName == null) return null
        return ReservationJuegoRefDto(nombre = normalizedName)
    }

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
     * Crea una reserva. La mesa se referencia por su [id] de base de datos
     * tal como exige el contrato actual del servidor (POST /reservas).
     */
    suspend fun createReservation(command: CreateReservationCommand) {
        when (command.role) {
            UserRole.USER -> reservationApi.createReservationAsUser(
                ReservationUserRequestDto(
                    fecha = command.fecha,
                    mesa = ReservationMesaIdRefDto(id = command.mesaId),
                    turno = ReservationTurnoRefDto(id = command.turnoId),
                    juego = command.toJuegoRefOrNull()
                )
            )

            UserRole.ADMIN -> reservationApi.createReservationAsAdmin(
                ReservationAdminRequestDto(
                    fecha = command.fecha,
                    mesa = ReservationMesaIdRefDto(id = command.mesaId),
                    turno = ReservationTurnoRefDto(id = command.turnoId),
                    juego = command.toJuegoRefOrNull(),
                    usuario = ReservationUsuarioRefDto(
                        nombre = command.usuarioNombre.orEmpty()
                    )
                )
            )
        }
    }
}


