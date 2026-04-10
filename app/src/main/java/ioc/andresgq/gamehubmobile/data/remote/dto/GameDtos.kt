package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * DTO de juego devuelto por el backend para el catálogo.
 *
 * Mantiene nombres de propiedades en inglés en la app, pero permite mapear
 * claves en español desde la API con @SerializedName.
 */
data class GameDto(
    val id: Long,
    val nombre: String,
    val numJugadores: String,
    val categoria: CategoriaDto,
    val disponible: Boolean,
    val descripcion: String? = null,
    val rutaImagen: String? = null
)

/**
 * DTO mínimo para la categoría asociada a un juego.
 *
 * Si el backend devuelve más campos dentro de `categoria`, Gson puede ignorarlos
 * sin problema mientras existan al menos estos.
 */
data class CategoriaDto(
    val id: Long,
    val nombre: String
)

/**
 * Respuesta típica para endpoint de listado: GET /games
 * (si el backend devuelve una lista JSON directa).
 */
typealias GameListResponseDto = List<GameDto>
