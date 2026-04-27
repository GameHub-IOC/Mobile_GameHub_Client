package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * Respuesta completa del mapa de ocupación diario.
 * Devuelta por GET /reservas/ocupacion?fecha=
 *
 * @property fecha fecha consultada en formato yyyy-MM-dd.
 * @property turnos lista de estados por cada turno del día.
 */
data class OcupacionResponseDto(
    val fecha: String,
    val turnos: List<TurnoEstadoDto> = emptyList()
)

/**
 * Estado de mesas y juegos en un turno específico.
 *
 * @property id id del turno.
 * @property nombre nombre del turno.
 * @property mesas lista detallada del estado de todas las mesas.
 * @property juegos lista de disponibilidad de todos los juegos.
 */
data class TurnoEstadoDto(
    val id: Long,
    val nombre: String,
    val mesas: List<MesaEstadoDto> = emptyList(),
    val juegos: List<JuegoEstadoDto> = emptyList()
)

/**
 * Detalle del estado de una mesa en un turno.
 *
 * @property numero número visible de la mesa.
 * @property estado "LIBRE" u "OCUPADA".
 * @property ocupadaPor nombre del usuario que tiene la reserva, o null si está libre.
 */
data class MesaEstadoDto(
    val numero: Int,
    val estado: String,
    val ocupadaPor: String? = null
) {
    val isOcupada: Boolean get() = estado == "OCUPADA"
}

/**
 * Disponibilidad de un título del catálogo en un turno.
 *
 * @property nombre nombre oficial del juego.
 * @property copiasTotales número total de copias físicas.
 * @property copiasLibres número de copias que no han sido reservadas aún.
 */
data class JuegoEstadoDto(
    val nombre: String,
    val copiasTotales: Long = 0L,
    val copiasLibres: Long = 0L
) {
    val hayDisponibilidad: Boolean get() = copiasLibres > 0L
}

