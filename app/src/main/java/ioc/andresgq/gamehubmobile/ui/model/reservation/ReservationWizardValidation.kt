@file:Suppress("unused")

package ioc.andresgq.gamehubmobile.ui.model.reservation

import ioc.andresgq.gamehubmobile.domain.reservation.UserRole

/**
 * Resultado simple de validación por paso.
 */
data class ReservationStepValidation(
    val isValid: Boolean,
    val message: String? = null
)

/**
 * Valida si se puede avanzar desde el paso actual.
 */
fun validateCurrentStep(state: ReservationWizardState): ReservationStepValidation {
    val draft = state.draft

    return when (state.currentStep) {
        ReservationWizardStep.DATE -> {
            if (draft.fecha.isBlank()) {
                ReservationStepValidation(false, "Selecciona una fecha")
            } else if (!DATE_REGEX.matches(draft.fecha.trim())) {
                ReservationStepValidation(false, "La fecha debe tener formato yyyy-MM-dd")
            } else {
                ReservationStepValidation(true)
            }
        }

        ReservationWizardStep.TURN -> {
            if (draft.turnoId == null) {
                ReservationStepValidation(false, "Selecciona un turno")
            } else {
                ReservationStepValidation(true)
            }
        }

        ReservationWizardStep.TABLE -> {
            val mesaId = draft.mesaId
            if (mesaId == null || mesaId <= 0L) {
                ReservationStepValidation(false, "Selecciona una mesa válida")
            } else {
                ReservationStepValidation(true)
            }
        }

        ReservationWizardStep.GAME -> {
            if (draft.juegoId == null) {
                ReservationStepValidation(false, "Selecciona un juego para continuar")
            } else {
                ReservationStepValidation(true)
            }
        }

        ReservationWizardStep.CONFIRMATION -> validateForSubmit(state)
    }
}

/**
 * Helper para habilitar o deshabilitar CTA de avanzar/confirmar.
 */
@Suppress("unused")
fun canContinue(state: ReservationWizardState): Boolean = validateCurrentStep(state).isValid

/**
 * Reglas mínimas para confirmar la reserva completa.
 */
fun validateForSubmit(state: ReservationWizardState): ReservationStepValidation {
    val draft = state.draft

    if (draft.fecha.isBlank()) {
        return ReservationStepValidation(false, "Falta la fecha")
    }
    if (!DATE_REGEX.matches(draft.fecha.trim())) {
        return ReservationStepValidation(false, "La fecha debe tener formato yyyy-MM-dd")
    }
    if (draft.turnoId == null) {
        return ReservationStepValidation(false, "Falta el turno")
    }
    if (draft.mesaId == null || draft.mesaId <= 0L) {
        return ReservationStepValidation(false, "Falta la mesa")
    }
    if (draft.juegoId == null) {
        return ReservationStepValidation(false, "Falta seleccionar un juego")
    }

    if (state.role == UserRole.ADMIN && draft.usuarioNombre.isNullOrBlank()) {
        return ReservationStepValidation(false, "Selecciona el usuario de la reserva")
    }

    return ReservationStepValidation(true)
}

private val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

