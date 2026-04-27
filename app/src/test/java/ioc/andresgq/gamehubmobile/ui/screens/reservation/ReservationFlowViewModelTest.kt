package ioc.andresgq.gamehubmobile.ui.screens.reservation

import ioc.andresgq.gamehubmobile.data.remote.ReservationApi
import ioc.andresgq.gamehubmobile.data.remote.ReservationRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationAdminRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationListItemDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUserRequestDto
import ioc.andresgq.gamehubmobile.data.repository.ReservationRepository
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.testutil.MainDispatcherRule
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationWizardStep
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationFlowViewModelTest {
    @Test
    fun init_loadsTurnOptions_andKeepsTableStateIdleUntilDateAndTurn() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi().apply {
            turns = listOf(ReservationTurnoDto(id = 1L, nombre = "Manana"))
            tables = listOf(ReservationMesaOperativaDto(numero = 3))
        }

        val viewModel = createViewModel(fakeApi = fakeApi)
        advanceUntilIdle()

        val turnsState = viewModel.turnOptionsState.value
        val tablesState = viewModel.tableOptionsState.value
        assertTrue(turnsState is UiState.Success)
        assertTrue(tablesState is UiState.Idle)
        assertEquals(1L, (turnsState as UiState.Success).data.first().id)
    }


    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun continueWithoutDate_staysOnDateAndShowsValidation() {
        val viewModel = createViewModel()

        viewModel.continueToNextStep()

        assertEquals(ReservationWizardStep.DATE, viewModel.wizardState.value.currentStep)
        assertEquals("Selecciona una fecha", viewModel.stepValidation.value?.message)
    }

    @Test
    fun continueThroughWizard_withValidSelections_reachesConfirmation() {
        val viewModel = createViewModel()

        viewModel.updateDate("2026-04-20")
        viewModel.continueToNextStep()
        viewModel.selectTurn(turnoId = 2L, turnoNombre = "Tarde")
        viewModel.continueToNextStep()
        viewModel.selectTable(6)
        viewModel.continueToNextStep()
        viewModel.selectGame(gameName = "Carcassonne", gameId = 10L)
        viewModel.continueToNextStep()

        assertEquals(ReservationWizardStep.CONFIRMATION, viewModel.wizardState.value.currentStep)
        assertNull(viewModel.stepValidation.value)
    }

    @Test
    fun continueWithUnavailableTurn_staysInTurnStep() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi().apply {
            turns = listOf(ReservationTurnoDto(id = 1L, nombre = "Manana"))
            tables = listOf(ReservationMesaOperativaDto(numero = 2))
        }
        val viewModel = createViewModel(fakeApi = fakeApi)
        advanceUntilIdle()

        viewModel.updateDate("2026-04-20")
        viewModel.continueToNextStep()
        viewModel.selectTurn(turnoId = 99L, turnoNombre = "Fuera")
        viewModel.continueToNextStep()

        assertEquals(ReservationWizardStep.TURN, viewModel.wizardState.value.currentStep)
        assertEquals(
            "El turno seleccionado ya no está disponible",
            viewModel.stepValidation.value?.message
        )
    }

    @Test
    fun submitReservation_forUser_successCallsRepository() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi().apply {
            tables = listOf(ReservationMesaOperativaDto(numero = 6))
        }
        val viewModel = createViewModel(fakeApi = fakeApi)

        viewModel.updateDate("2026-04-20")
        viewModel.selectTurn(turnoId = 2L, turnoNombre = "Tarde")
        viewModel.selectTable(6)
        viewModel.selectGame(gameName = "Carcassonne", gameId = 10L)
        viewModel.submitReservation()
        advanceUntilIdle()

        assertTrue(viewModel.submitState.value is UiState.Success)
        assertEquals("2026-04-20", fakeApi.lastUserRequest?.fecha)
        assertEquals(6, fakeApi.lastUserRequest?.mesa?.numero)
        assertEquals(2L, fakeApi.lastUserRequest?.turno?.id)
        assertEquals("Carcassonne", fakeApi.lastUserRequest?.juego?.nombre)
    }

    @Test
    fun submitReservation_forAdminWithoutUser_failsValidationBeforeApi() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi()
        val viewModel = createViewModel(role = UserRole.ADMIN, fakeApi = fakeApi)

        viewModel.updateDate("2026-04-20")
        viewModel.selectTurn(turnoId = 2L, turnoNombre = "Tarde")
        viewModel.selectTable(6)
        viewModel.submitReservation()
        advanceUntilIdle()

        val state = viewModel.submitState.value
        assertTrue(state is UiState.Error)
        assertEquals(
            "Selecciona el usuario de la reserva",
            (state as UiState.Error).message
        )
        assertNull(fakeApi.lastAdminRequest)
    }

    @Test
    fun goBackFromConfirmation_returnsToGameStep() {
        val viewModel = createViewModel()

        viewModel.updateDate("2026-04-20")
        viewModel.continueToNextStep()
        viewModel.selectTurn(turnoId = 2L)
        viewModel.continueToNextStep()
        viewModel.selectTable(6)
        viewModel.continueToNextStep()
        viewModel.continueToNextStep()
        viewModel.goToPreviousStep()

        assertEquals(ReservationWizardStep.GAME, viewModel.wizardState.value.currentStep)
    }

    private fun createViewModel(
        role: UserRole = UserRole.USER,
        fakeApi: FakeReservationApi = FakeReservationApi()
    ): ReservationFlowViewModel {
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return ReservationFlowViewModel(
            reservationRepository = repository,
            role = role
        )
    }

    private class FakeReservationApi : ReservationApi {
        var lastUserRequest: ReservationUserRequestDto? = null
        var lastAdminRequest: ReservationAdminRequestDto? = null
        var myReservations: List<ReservationListItemDto> = emptyList()
        var adminReservations: List<ReservationListItemDto> = emptyList()
        var turns: List<ReservationTurnoDto> = emptyList()
        var tables: List<ReservationMesaOperativaDto> = emptyList()

        override suspend fun getMyReservations(): List<ReservationListItemDto> = myReservations

        override suspend fun getAdminReservations(): List<ReservationListItemDto> = adminReservations

        override suspend fun getTurns(): List<ReservationTurnoDto> = turns

        override suspend fun getOperationalTables(): List<ReservationMesaOperativaDto> = tables

        override suspend fun getFreeTables(fecha: String, turnoId: Long): List<ReservationMesaOperativaDto> = tables

        override suspend fun createReservationAsUser(request: ReservationUserRequestDto) {
            lastUserRequest = request
        }

        override suspend fun createReservationAsAdmin(request: ReservationAdminRequestDto) {
            lastAdminRequest = request
        }
    }
}

