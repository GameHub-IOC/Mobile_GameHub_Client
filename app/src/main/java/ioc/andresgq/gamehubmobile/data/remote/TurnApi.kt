package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Define los endpoints remotos de gestión de turnos del local.
 *
 * Operaciones de lectura (GET) son públicas.
 * Operaciones de escritura (POST / PUT / DELETE) requieren rol ADMIN.
 */
interface TurnApi {

    /** Retorna el listado completo de turnos disponibles. */
    @GET("turnos")
    suspend fun getTurnos(): List<ReservationTurnoDto>

    /**
     * Crea un nuevo turno. Requiere rol ADMIN.
     * Devuelve 400 si ya existe un turno con el mismo nombre.
     *
     * @param turno datos del nuevo turno (nombre, horaInicio, horaFin).
     */
    @POST("turnos")
    suspend fun crearTurno(@Body turno: ReservationTurnoDto): ReservationTurnoDto

    /**
     * Actualiza un turno existente. El ID del turno viaja en el body. Requiere rol ADMIN.
     *
     * @param turno turno con el id, nombre y horas actualizados.
     */
    @PUT("turnos")
    suspend fun actualizarTurno(@Body turno: ReservationTurnoDto): ReservationTurnoDto

    /**
     * Elimina un turno por su id. Requiere rol ADMIN.
     * Devuelve 400 si el turno tiene reservas activas asociadas.
     *
     * @param id id del turno a eliminar.
     */
    @DELETE("turnos/{id}")
    suspend fun eliminarTurnoPorId(@Path("id") id: Long)
}

