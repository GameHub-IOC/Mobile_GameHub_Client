package ioc.andresgq.gamehubmobile.data.remote

import ioc.andresgq.gamehubmobile.data.remote.dto.GameListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Define los endpoints remotos relacionados con el catálogo de juegos.
 *
 * Esta interfaz es utilizada por Retrofit para generar la implementación
 * concreta de las llamadas HTTP asociadas a la consulta de juegos.
 */
interface GameApi {

    /**
     * Recupera el listado completo de juegos del catálogo.
     *
     * La petición se realiza mediante `GET` al endpoint `juegos`.
     * Se espera que el backend devuelva un array JSON directo
     * compatible con [GameListResponseDto].
     *
     * @return lista de juegos devueltos por el servidor.
     */
    @GET("juegos")
    suspend fun getGames(): GameListResponseDto

    @GET("juegos/categoria/{nom}")
    suspend fun getGamesByCategory(
        @Path("nom") categoryName: String
    ): GameListResponseDto

    @GET("juegos/disponibles")
    suspend fun getAvailableGames(): GameListResponseDto
}