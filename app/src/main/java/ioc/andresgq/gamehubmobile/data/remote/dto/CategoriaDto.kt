package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * DTO de categoría de juegos devuelto y enviado al backend.
 *
 * @property id id único de la categoría (null al crear una nueva).
 * @property nombre nombre de la categoría (único en el sistema).
 */
data class CategoriaDto(
    val id: Long = 0L,
    val nombre: String
)

