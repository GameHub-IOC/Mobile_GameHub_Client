package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.OcupacionResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationAdminRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationListItemDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUserRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Contrato Retrofit para operaciones de reservas.
 */
interface ReservationApi {

    /**
     * Devuelve las reservas del usuario autenticado.
     */
    @GET("reservas/mis-reservas")
    suspend fun getMyReservations(): List<ReservationListItemDto>

    /**
     * Devuelve todas las reservas para contexto administrativo.
     */
    @GET("reservas")
    suspend fun getAdminReservations(): List<ReservationListItemDto>

    /**
     * Devuelve los turnos disponibles, incluyendo horaInicio y horaFin.
     */
    @GET("turnos")
    suspend fun getTurns(): List<ReservationTurnoDto>

    /**
     * Devuelve mesas operativas (id, numero, capacidad, operativa).
     */
    @GET("mesas/operativas")
    suspend fun getOperationalTables(): List<ReservationMesaOperativaDto>

    /**
     * Devuelve mesas libres para una fecha y turno concretos.
     */
    @GET("mesas/libres")
    suspend fun getFreeTables(
        @Query("fecha") fecha: String,
        @Query("turnoId") turnoId: Long
    ): List<ReservationMesaOperativaDto>

    /**
     * Devuelve el mapa de ocupación diario: estado LIBRE/OCUPADA de todas las mesas
     * y disponibilidad de juegos para cada turno de una fecha dada.
     *
     * @param fecha fecha a consultar en formato yyyy-MM-dd.
     */
    @GET("reservas/ocupacion")
    suspend fun getOcupacion(
        @Query("fecha") fecha: String
    ): OcupacionResponseDto

    /**
     * Crea una reserva en contexto de usuario estándar.
     */
    @POST("reservas")
    suspend fun createReservationAsUser(
        @Body request: ReservationUserRequestDto
    )

    /**
     * Crea una reserva en contexto de administración asignando usuario destino.
     */
    @POST("reservas")
    suspend fun createReservationAsAdmin(
        @Body request: ReservationAdminRequestDto
    )
}
