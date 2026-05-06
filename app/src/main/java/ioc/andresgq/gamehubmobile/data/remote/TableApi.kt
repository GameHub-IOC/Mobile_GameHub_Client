package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Define los endpoints remotos de gestión de mesas del local.
 *
 * Operaciones de lectura (GET) son públicas.
 * Operaciones de escritura (POST / PUT / DELETE) requieren rol ADMIN.
 */
interface TableApi {

    /** Retorna el listado completo de mesas (todas, operativas y no operativas). */
    @GET("mesas")
    suspend fun getMesas(): List<ReservationMesaOperativaDto>

    /**
     * Crea una nueva mesa en el local. Requiere rol ADMIN.
     *
     * @param mesa datos de la nueva mesa (numero, capacidad, operativa).
     */
    @POST("mesas")
    suspend fun crearMesa(@Body mesa: ReservationMesaOperativaDto): ReservationMesaOperativaDto

    /**
     * Actualiza una mesa existente por su id. Requiere rol ADMIN.
     *
     * @param id   id de la mesa a actualizar.
     * @param mesa nuevos datos de la mesa.
     */
    @PUT("mesas/{id}")
    suspend fun actualizarMesaPorId(
        @Path("id") id: Long,
        @Body mesa: ReservationMesaOperativaDto
    ): ReservationMesaOperativaDto

    /**
     * Elimina una mesa por su id. Requiere rol ADMIN.
     * Devuelve 400 si tiene reservas asociadas.
     *
     * @param id id de la mesa a eliminar.
     */
    @DELETE("mesas/{id}")
    suspend fun eliminarMesaPorId(@Path("id") id: Long)
}

