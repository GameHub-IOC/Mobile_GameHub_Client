package ioc.andresgq.gamehubmobile.ui.screens.reservations

/**
 * Modelo de UI para representar una reserva en listados.
 */
data class ReservationListItemUi(
    val id: Long,
    val fecha: String,
    val estado: String,
    val mesaNumero: Int?,
    val turnoNombre: String,
    val juegoNombre: String,
    val usuarioNombre: String
)

