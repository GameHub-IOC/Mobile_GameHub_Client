package ioc.andresgq.gamehubmobile.ui.model.reservation

import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationWizardValidationTest {

    @Test
    fun dateStep_withoutDate_isInvalid() {
        val state = ReservationWizardState(
            role = UserRole.USER,
            currentStep = ReservationWizardStep.DATE,
            draft = ReservationDraft(fecha = "")
        )

        assertFalse(canContinue(state))
    }

    @Test
    fun tableStep_withInvalidTable_isInvalid() {
        val state = ReservationWizardState(
            role = UserRole.USER,
            currentStep = ReservationWizardStep.TABLE,
            draft = ReservationDraft(
                fecha = "2026-04-11",
                turnoId = 2L,
                mesaNumero = 0
            )
        )

        assertFalse(canContinue(state))
    }

    @Test
    fun confirmation_forUser_withRequiredData_isValid() {
        val state = ReservationWizardState(
            role = UserRole.USER,
            currentStep = ReservationWizardStep.CONFIRMATION,
            draft = ReservationDraft(
                fecha = "2026-04-11",
                turnoId = 2L,
                mesaNumero = 4
            )
        )

        assertTrue(canContinue(state))
    }

    @Test
    fun confirmation_forAdmin_withoutTargetUser_isInvalid() {
        val state = ReservationWizardState(
            role = UserRole.ADMIN,
            currentStep = ReservationWizardStep.CONFIRMATION,
            draft = ReservationDraft(
                fecha = "2026-04-11",
                turnoId = 2L,
                mesaNumero = 4,
                usuarioNombre = ""
            )
        )

        assertFalse(canContinue(state))
    }

    @Test
    fun confirmation_forAdmin_withTargetUser_isValid() {
        val state = ReservationWizardState(
            role = UserRole.ADMIN,
            currentStep = ReservationWizardStep.CONFIRMATION,
            draft = ReservationDraft(
                fecha = "2026-04-11",
                turnoId = 2L,
                mesaNumero = 4,
                usuarioNombre = "carlos"
            )
        )

        assertTrue(canContinue(state))
    }
}

