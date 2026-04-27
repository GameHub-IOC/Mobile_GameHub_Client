package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * DTO de listado de reservas para usuario y administración.
 *
 * @property id identificador único de la reserva.
 * @property fecha fecha de la reserva.
 * @property estado estado actual de la reserva.
 * @property mesa información mínima de mesa para listados.
 * @property turno información mínima de turno para listados.
 * @property juego información mínima de juego para listados.
 * @property usuario información mínima de usuario para listados.
 */
data class ReservationListItemDto(
    val id: Long,
    val fecha: String,
    val estado: String? = null,
    val mesa: ReservationMesaRefDto? = null,
    val turno: ReservationTurnoListDto? = null,
    val juego: ReservationJuegoListDto? = null,
    val usuario: ReservationUsuarioRefDto? = null
)

/** Información mínima de turno para listados.
 *
 * @property id identificador único del turno.
 * @property nombre nombre del turno.
 */
data class ReservationTurnoListDto(
    val id: Long? = null,
    val nombre: String? = null
)

/** Información mínima de juego para listados.
 *
 * @property id identificador único del juego.
 * @property nombre nombre del juego.
 */
data class ReservationJuegoListDto(
    val id: Long? = null,
    val nombre: String? = null
)

