package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody

/**
 * Fuente de datos remota para catálogo de juegos.
 */
class GameRemoteDataSource(
    private val gameApi: GameApi
) {
    suspend fun getGames(): List<GameDto> = gameApi.getGames()

    suspend fun getGamesByCategory(categoryName: String): List<GameDto> =
        gameApi.getGamesByCategory(categoryName)

    suspend fun getAvailableGames(): List<GameDto> = gameApi.getAvailableGames()

    suspend fun getGameById(id: Long): GameDto = gameApi.getGameById(id)

    /**
     * Devuelve todas las copias físicas de un juego por nombre.
     */
    suspend fun getGamesByName(nombre: String): List<GameDto> =
        gameApi.getGamesByName(nombre)

    /**
     * Retorna el recurso binario de la imagen de un juego.
     * Usar con Coil/Glide pasando la URL completa: baseUrl + "juegos/imagen/" + rutaImagen.
     */
    suspend fun getGameImage(nombreArchivo: String): ResponseBody =
        gameApi.getGameImage(nombreArchivo)

    /**
     * Sube una imagen y la asocia al juego indicado. Requiere rol ADMIN.
     *
     * @param id id del juego.
     * @param archivo parte multipart de la imagen.
     */
    suspend fun uploadGameImage(id: Long, archivo: MultipartBody.Part): GameDto =
        gameApi.uploadGameImage(id, archivo)
}

