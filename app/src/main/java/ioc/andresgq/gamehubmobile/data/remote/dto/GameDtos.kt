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
    val observaciones: String? = null,
    val rutaImagen: String? = null
)


/**
 * Respuesta típica para endpoint de listado: GET /games
 * (si el backend devuelve una lista JSON directa).
 */
typealias GameListResponseDto = List<GameDto>

/**
 * Cuerpo de petición para crear o actualizar un juego.
 *
 * No incluye [id] (asignado por el servidor) ni [rutaImagen]
 * (se gestiona de forma separada con el endpoint de subida de imagen).
 */
data class GameRequestDto(
    val nombre: String,
    val numJugadores: String,
    val categoria: CategoriaDto,
    val disponible: Boolean,
    val descripcion: String? = null,
    val observaciones: String? = null
)

