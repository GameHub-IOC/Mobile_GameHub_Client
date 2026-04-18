package ioc.andresgq.gamehubmobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoriaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto

/**
 * Entidad de Room que representa un juego en la caché local.
 *
 * Almacena los datos relevantes de [GameDto], aplanando el objeto anidado
 * [CategoriaDto] en dos columnas simples ([categoriaId] y [categoriaNombre])
 * para cumplir con las restricciones de Room sobre tipos primitivos.
 *
 * @property id identificador único del juego, usado como clave primaria.
 * @property nombre nombre del juego.
 * @property numJugadores número de jugadores admitidos.
 * @property categoriaId identificador de la categoría a la que pertenece el juego.
 * @property categoriaNombre nombre de la categoría del juego.
 * @property disponible indica si el juego está disponible para reserva.
 * @property descripcion descripción opcional del juego.
 * @property rutaImagen ruta o URL opcional de la imagen del juego.
 */
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val numJugadores: String,
    val categoriaId: Long,
    val categoriaNombre: String,
    val disponible: Boolean,
    val descripcion: String?,
    val observaciones: String? = null,
    val rutaImagen: String?
)

/**
 * Convierte un [GameDto] recibido de la API en un [GameEntity] apto para Room.
 */
fun GameDto.toEntity(): GameEntity = GameEntity(
    id = id,
    nombre = nombre,
    numJugadores = numJugadores,
    categoriaId = categoria.id,
    categoriaNombre = categoria.nombre,
    disponible = disponible,
    descripcion = descripcion,
    observaciones = observaciones,
    rutaImagen = rutaImagen
)

/**
 * Reconstituye un [GameDto] a partir de un [GameEntity] almacenado en caché.
 */
fun GameEntity.toDto(): GameDto = GameDto(
    id = id,
    nombre = nombre,
    numJugadores = numJugadores,
    categoria = CategoriaDto(id = categoriaId, nombre = categoriaNombre),
    disponible = disponible,
    descripcion = descripcion,
    observaciones = observaciones,
    rutaImagen = rutaImagen
)