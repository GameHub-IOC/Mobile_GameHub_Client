package ioc.andresgq.gamehubmobile.domain.reservation

/**
 * Entrada de dominio para crear una reserva.
 *
 * Mantiene los datos necesarios para construir el request real según el rol.
 * La mesa se referencia por [mesaId] (id de BD) tal como exige el contrato del servidor.
 * El [mesaNumero] se conserva para validación y mensajes de UI (no se envía al servidor).
 */
data class CreateReservationCommand(
    val role: UserRole,
    val fecha: String,
    val turnoId: Long,
    /** Id de base de datos de la mesa. Se envía en el body del POST /reservas. */
    val mesaId: Long,
    /** Número visible de la mesa. Solo se usa para validación y mensajes de error en UI. */
    val mesaNumero: Int = 0,
    val juegoNombre: String? = null,
    val usuarioNombre: String? = null
)

