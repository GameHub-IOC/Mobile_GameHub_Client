package ioc.andresgq.gamehubmobile.ui.screens.profile

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
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_withStoredSession_exposesProfileInfo() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token", "andres", "ADMIN")
        }
        val viewModel = createViewModel(fakeDao)

        advanceUntilIdle()

        val state = viewModel.profileState.value
        assertTrue(state is UiState.Success)
        assertEquals(
            ProfileInfo("andres", "ADMIN"),
            (state as UiState.Success).data
        )
    }

    @Test
    fun init_withoutSession_exposesError() = runTest(mainDispatcherRule.scheduler) {
        val viewModel = createViewModel(FakeUserSessionDao())

        advanceUntilIdle()

        val state = viewModel.profileState.value
        assertTrue(state is UiState.Error)
        assertEquals("No hay ninguna sesión activa", (state as UiState.Error).message)
    }

    @Test
    fun logout_success_updatesStateAndClearsSession() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token", "pepe", "USER")
        }
        val viewModel = createViewModel(fakeDao)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertTrue(viewModel.logoutState.value is UiState.Success)
        assertEquals(1, fakeDao.clearCalls)
    }

    @Test
    fun logout_failure_exposesErrorState() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            clearThrowable = IllegalStateException("db down")
        }
        val viewModel = createViewModel(fakeDao)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        val state = viewModel.logoutState.value
        assertTrue(state is UiState.Error)
        assertEquals("No se pudo cerrar sesión", (state as UiState.Error).message)
    }

    private fun createViewModel(fakeDao: FakeUserSessionDao): ProfileViewModel {
        val repository = AuthRepository(
            authApi = FakeAuthApi(),
            tokenManager = TokenManager(fakeDao),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return ProfileViewModel(repository)
    }
}

