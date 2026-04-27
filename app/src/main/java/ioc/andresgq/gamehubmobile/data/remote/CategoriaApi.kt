package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.CategoriaDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Define los endpoints remotos de gestión de categorías de juegos.
 *
 * Operaciones de lectura (GET) son públicas.
 * Operaciones de escritura requieren rol ADMIN.
 */
interface CategoriaApi {

    /** Retorna el listado completo de categorías. */
    @GET("categorias")
    suspend fun getCategorias(): List<CategoriaDto>

    /** Busca una categoría por su id. */
    @GET("categorias/{id}")
    suspend fun getCategoriaById(@Path("id") id: Long): CategoriaDto

    /** Busca una categoría por su nombre. */
    @GET("categorias/{nombre}")
    suspend fun getCategoriaByNombre(@Path("nombre") nombre: String): CategoriaDto

    /**
     * Crea una nueva categoría. Requiere rol ADMIN.
     *
     * @param categoria datos de la nueva categoría.
     */
    @POST("categorias")
    suspend fun crearCategoria(@Body categoria: CategoriaDto): CategoriaDto

    /**
     * Actualiza una categoría existente por id. Requiere rol ADMIN.
     *
     * @param id id de la categoría a actualizar.
     * @param categoria nuevos datos de la categoría.
     */
    @PUT("categorias/{id}")
    suspend fun actualizarCategoriaPorId(
        @Path("id") id: Long,
        @Body categoria: CategoriaDto
    ): CategoriaDto

    /**
     * Actualiza una categoría existente por nombre. Requiere rol ADMIN.
     *
     * @param nombreActual nombre actual de la categoría.
     * @param categoria nuevos datos de la categoría.
     */
    @PUT("categorias/{nombreActual}")
    suspend fun actualizarCategoriaPorNombre(
        @Path("nombreActual") nombreActual: String,
        @Body categoria: CategoriaDto
    ): CategoriaDto

    /**
     * Borra una categoría por nombre. Los juegos asociados quedan sin categoría.
     * Requiere rol ADMIN.
     *
     * @param nombre nombre de la categoría a eliminar.
     */
    @DELETE("categorias/nombre/{nombre}")
    suspend fun eliminarCategoriaPorNombre(@Path("nombre") nombre: String)
}

