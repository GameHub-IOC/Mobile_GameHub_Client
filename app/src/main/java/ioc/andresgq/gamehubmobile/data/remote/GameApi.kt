package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameListResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameRequestDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Define los endpoints remotos relacionados con el catálogo de juegos.
 */
interface GameApi {

    /** Recupera el listado completo de juegos del catálogo. */
    @GET("juegos")
    suspend fun getGames(): GameListResponseDto

    /** Filtra juegos por nombre de categoría. */
    @GET("juegos/categoria/{nom}")
    suspend fun getGamesByCategory(
        @Path("nom") categoryName: String
    ): GameListResponseDto

    /** Retorna solo los juegos con disponibilidad = true. */
    @GET("juegos/disponibles")
    suspend fun getAvailableGames(): GameListResponseDto

    /** Recupera un juego específico por su identificador. */
    @GET("juegos/{id}")
    suspend fun getGameById(@Path("id") id: Long): GameDto

    /**
     * Devuelve todas las copias físicas de un juego con el mismo nombre.
     *
     * @param nombre nombre del juego a buscar.
     */
    @GET("juegos/nombre/{nombre}")
    suspend fun getGamesByName(@Path("nombre") nombre: String): GameListResponseDto

    /**
     * Retorna el recurso binario de la imagen de un juego (para cargar con Coil/Glide).
     * La URL completa se construye como: baseUrl + "juegos/imagen/" + rutaImagen del juego.
     *
     * @param nombreArchivo nombre del archivo tal como está en [GameDto.rutaImagen].
     */
    @GET("juegos/imagen/{nombreArchivo}")
    suspend fun getGameImage(@Path("nombreArchivo") nombreArchivo: String): ResponseBody

    /**
     * Sube un archivo de imagen y lo asocia al juego indicado. Requiere rol ADMIN.
     *
     * @param id id del juego al que se asocia la imagen.
     * @param archivo parte multipart que contiene el archivo de imagen.
     */
    @Multipart
    @POST("juegos/{id}/subir-imagen")
    suspend fun uploadGameImage(
        @Path("id") id: Long,
        @Part archivo: MultipartBody.Part
    ): GameDto

    /**
     * Crea un nuevo juego en el catálogo. Requiere rol ADMIN.
     *
     * @param game datos del juego a crear (sin id ni rutaImagen).
     */
    @POST("juegos")
    suspend fun createGame(@Body game: GameRequestDto): GameDto

    /**
     * Actualiza los datos de un juego existente. Requiere rol ADMIN.
     *
     * @param id  id del juego a actualizar.
     * @param game nuevos datos del juego.
     */
    @PUT("juegos/{id}")
    suspend fun updateGame(
        @Path("id") id: Long,
        @Body game: GameRequestDto
    ): GameDto

    /**
     * Elimina un juego del catálogo. Requiere rol ADMIN.
     *
     * @param id id del juego a eliminar.
     */
    @DELETE("juegos/{id}")
    suspend fun deleteGame(@Path("id") id: Long)
}
