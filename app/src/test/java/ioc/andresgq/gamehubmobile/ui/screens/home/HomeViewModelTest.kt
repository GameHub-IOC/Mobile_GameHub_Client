package ioc.andresgq.gamehubmobile.ui.screens.home

import ioc.andresgq.gamehubmobile.data.local.TokenManager
import ioc.andresgq.gamehubmobile.data.local.UserSessionEntity
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.testutil.FakeAuthApi
import ioc.andresgq.gamehubmobile.testutil.FakeUserSessionDao
import ioc.andresgq.gamehubmobile.testutil.MainDispatcherRule
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun logout_success_setsSuccessState() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token", "andres", "USER")
        }
        val viewModel = createViewModel(fakeDao)

        viewModel.logout()
        advanceUntilIdle()

        assertTrue(viewModel.logoutState.value is UiState.Success)
        assertEquals(1, fakeDao.clearCalls)
    }

    @Test
    fun logout_daoFailure_setsErrorState() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            clearThrowable = IllegalStateException("db failure")
        }
        val viewModel = createViewModel(fakeDao)

        viewModel.logout()
        advanceUntilIdle()

        val state = viewModel.logoutState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("cerrar"))
    }

    @Test
    fun getCurrentUser_mapsSessionToHomeInfo() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token", "pepe", "ADMIN")
        }
        val viewModel = createViewModel(fakeDao)

        val user = viewModel.getCurrentUser()

        assertEquals(HomeInfo("pepe", "ADMIN"), user)
    }

    @Test
    fun getCurrentUser_withoutSession_returnsNull() = runTest(mainDispatcherRule.scheduler) {
        val viewModel = createViewModel(FakeUserSessionDao())

        val user = viewModel.getCurrentUser()

        assertEquals(null, user)
    }

    private fun createViewModel(fakeDao: FakeUserSessionDao): HomeViewModel {
        val repository = AuthRepository(
            authApi = FakeAuthApi(),
            tokenManager = TokenManager(fakeDao),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return HomeViewModel(repository)
    }
}


