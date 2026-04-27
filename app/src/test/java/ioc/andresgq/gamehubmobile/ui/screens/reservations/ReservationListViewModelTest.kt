package ioc.andresgq.gamehubmobile.ui.screens.reservations

import ioc.andresgq.gamehubmobile.data.remote.ReservationApi
import ioc.andresgq.gamehubmobile.data.remote.ReservationRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationAdminRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationJuegoListDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationListItemDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaRefDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoListDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUserRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUsuarioRefDto
import ioc.andresgq.gamehubmobile.data.repository.ReservationRepository
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.testutil.MainDispatcherRule
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadMyReservations_success_updatesState() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi().apply {
            myReservations = listOf(
                ReservationListItemDto(
                    id = 1L,
                    fecha = "2026-04-10",
                    estado = "PENDIENTE",
                    mesa = ReservationMesaRefDto(2),
                    turno = ReservationTurnoListDto(id = 1L, nombre = "Manana"),
                    juego = ReservationJuegoListDto(id = 10L, nombre = "Azul"),
                    usuario = ReservationUsuarioRefDto(nombre = "ana")
                )
            )
        }
        val viewModel = createViewModel(fakeApi)

        viewModel.loadMyReservations()
        advanceUntilIdle()

        val state = viewModel.myReservationsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1L, (state as UiState.Success).data.first().id)
    }

    @Test
    fun loadAdminReservations_success_updatesState() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi().apply {
            adminReservations = listOf(
                ReservationListItemDto(
                    id = 5L,
                    fecha = "2026-04-11",
                    usuario = ReservationUsuarioRefDto(nombre = "mario")
                )
            )
        }
        val viewModel = createViewModel(fakeApi)

        viewModel.loadAdminReservations()
        advanceUntilIdle()

        val state = viewModel.adminReservationsState.value
        assertTrue(state is UiState.Success)
        assertEquals("mario", (state as UiState.Success).data.first().usuarioNombre)
    }

    @Test
    fun loadMyReservations_whenNetworkFails_setsError() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi().apply {
            myReservationsThrowable = IOException("offline")
        }
        val viewModel = createViewModel(fakeApi)

        viewModel.loadMyReservations()
        advanceUntilIdle()

        val state = viewModel.myReservationsState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("conectar"))
    }

    @Test
    fun refreshForRole_dispatchesToAdminLoader() = runTest(mainDispatcherRule.scheduler) {
        val fakeApi = FakeReservationApi().apply {
            adminReservations = listOf(
                ReservationListItemDto(id = 99L, fecha = "2026-04-12")
            )
        }
        val viewModel = createViewModel(fakeApi)

        viewModel.refreshForRole(UserRole.ADMIN)
        advanceUntilIdle()

        val state = viewModel.adminReservationsState.value
        assertTrue(state is UiState.Success)
        assertEquals(99L, (state as UiState.Success).data.first().id)
    }

    private fun createViewModel(fakeApi: FakeReservationApi): ReservationListViewModel {
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return ReservationListViewModel(repository)
    }

    private class FakeReservationApi : ReservationApi {
        var myReservations: List<ReservationListItemDto> = emptyList()
        var adminReservations: List<ReservationListItemDto> = emptyList()
        var myReservationsThrowable: Throwable? = null
        var adminReservationsThrowable: Throwable? = null

        override suspend fun getMyReservations(): List<ReservationListItemDto> {
            myReservationsThrowable?.let { throw it }
            return myReservations
        }

        override suspend fun getAdminReservations(): List<ReservationListItemDto> {
            adminReservationsThrowable?.let { throw it }
            return adminReservations
        }

        override suspend fun getTurns(): List<ReservationTurnoDto> = emptyList()

        override suspend fun getOperationalTables(): List<ReservationMesaOperativaDto> = emptyList()

        override suspend fun getFreeTables(fecha: String, turnoId: Long): List<ReservationMesaOperativaDto> = emptyList()

        override suspend fun createReservationAsUser(request: ReservationUserRequestDto) = Unit

        override suspend fun createReservationAsAdmin(request: ReservationAdminRequestDto) = Unit
    }
}

