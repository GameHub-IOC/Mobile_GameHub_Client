package ioc.andresgq.gamehubmobile.ui.model.reservation

import ioc.andresgq.gamehubmobile.domain.reservation.UserRole

/**
 * Pasos canónicos del wizard de reserva.
 */
enum class ReservationWizardStep {
    DATE,
    TURN,
    TABLE,
    GAME,
    CONFIRMATION
}

/**
 * Borrador editable durante el wizard.
 *
 * @property mesaNumero número visible de la mesa (para mostrar en UI y validar contra lista cargada).
 * @property mesaId id de base de datos de la mesa (para enviar en el POST /reservas).
 */
data class ReservationDraft(
    val fecha: String = "",
    val turnoId: Long? = null,
    val turnoNombre: String? = null,
    val mesaNumero: Int? = null,
    val mesaId: Long? = null,
    val juegoId: Long? = null,
    val juegoNombre: String? = null,
    val juegoEtiqueta: String? = null,
    val usuarioNombre: String? = null
)

/**
 * Opción de turno lista para renderizar en el wizard.
 *
 * @property horaInicio hora de inicio formateada "HH:mm" o null si el servidor no la devuelve.
 * @property horaFin hora de fin formateada "HH:mm" o null si el servidor no la devuelve.
 */
data class ReservationTurnOption(
    val id: Long,
    val nombre: String,
    val horaInicio: String? = null,
    val horaFin: String? = null
)

/**
 * Opción de mesa lista para renderizar en el wizard (mapa visual o lista).
 *
 * @property id id de base de datos (se usa al crear la reserva).
 * @property numero número visible en el local.
 * @property capacidad capacidad máxima de jugadores.
 */
data class ReservationTableOption(
    val id: Long,
    val numero: Int,
    val capacidad: Int = 0
)

/**
 * Estado completo para la pantalla de wizard.
 */
data class ReservationWizardState(
    val role: UserRole,
    val currentStep: ReservationWizardStep = ReservationWizardStep.DATE,
    val draft: ReservationDraft = ReservationDraft()
)

