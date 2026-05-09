package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * Cuerpo para crear una reserva como usuario normal.
 *
 * Se envían AMBOS formatos de identificación que acepta el servidor:
 *  · mesaNumero + turnoNombre  → formato primario (ejemplos 1 y 2 del Swagger)
 *  · mesaId     + turnoId      → formato de compatibilidad (ejemplo 3)
 * Así el servidor puede usar el que encuentre disponible sin lanzar NPE.
 */
data class ReservationUserRequestDto(
    val fecha: String,
    val mesaId: Long,
    val mesaNumero: Int,
    val turnoId: Long,
    val turnoNombre: String? = null,
    val juegoNombre: String? = null
)

/**
 * Cuerpo para crear una reserva como administrador.
 *
 * Igual que [ReservationUserRequestDto] pero añade [usuarioNombre] (solo ADMIN).
 */
data class ReservationAdminRequestDto(
    val fecha: String,
    val mesaId: Long,
    val mesaNumero: Int,
    val turnoId: Long,
    val turnoNombre: String? = null,
    val juegoNombre: String? = null,
    val usuarioNombre: String? = null
)

/**
 * Referencia de mesa usada en respuestas de reservas (listados).
 * Contiene todos los campos que el servidor puede devolver dentro de un objeto Reserva.
 */
data class ReservationMesaRefDto(
    val id: Long? = null,
    val numero: Int? = null,
    val capacidad: Int? = null,
    val operativa: Boolean? = null
)

/** Referencia mínima de turno para respuestas de listado. */
data class ReservationTurnoRefDto(
    val id: Long? = null,
    val nombre: String? = null
)

/** Referencia mínima de juego para respuestas de listado. */
data class ReservationJuegoRefDto(
    val nombre: String
)

/** Referencia mínima de usuario para respuestas de listado. */
data class ReservationUsuarioRefDto(
    val nombre: String
)

