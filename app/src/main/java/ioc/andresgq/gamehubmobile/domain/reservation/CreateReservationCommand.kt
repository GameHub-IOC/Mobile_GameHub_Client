package ioc.andresgq.gamehubmobile.domain.reservation

/**
 * Entrada de dominio para crear una reserva.
 *
 * Mantiene TODOS los identificadores disponibles para mesa y turno de forma que el servidor
 * pueda usar cualquiera de los dos formatos soportados (por ID o por nombre/número).
 *
 * El contrato del servidor (POST /reservas) acepta:
 *  · mesaNumero + turnoNombre  → formato primario (ejemplos 1 y 2 del Swagger)
 *  · mesaId + turnoId          → formato de compatibilidad (ejemplo 3)
 * Enviamos ambos para evitar NPE si el servidor intenta acceder al campo que no se envió.
 */
data class CreateReservationCommand(
    val role: UserRole,
    val fecha: String,
    /** Id de base de datos del turno (compatibilidad). */
    val turnoId: Long,
    /** Nombre canónico del turno, p.ej. "TARDE_1" (formato primario del servidor). */
    val turnoNombre: String? = null,
    /** Id de base de datos de la mesa (compatibilidad). */
    val mesaId: Long,
    /** Número visible de la mesa en el local (formato primario del servidor). */
    val mesaNumero: Int = 0,
    val juegoNombre: String? = null,
    val usuarioNombre: String? = null
)

