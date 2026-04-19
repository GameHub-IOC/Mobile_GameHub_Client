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
class ProfileSessionCompatibilityTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun logout_thenConsumeState_returnsIdle() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token", "andres", "USER")
        }
        val viewModel = createViewModel(fakeDao)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()
        assertTrue(viewModel.logoutState.value is UiState.Success)

        viewModel.consumeLogoutState()

        assertEquals(UiState.Idle, viewModel.logoutState.value)
    }

    @Test
    fun loadProfile_afterSessionChange_refreshesProfileInfo() = runTest(mainDispatcherRule.scheduler) {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token", "pepe", "USER")
        }
        val viewModel = createViewModel(fakeDao)
        advanceUntilIdle()

        fakeDao.storedSession = UserSessionEntity(0, "token", "ana", "ADMIN")
        viewModel.loadProfile()
        advanceUntilIdle()

        val state = viewModel.profileState.value
        assertTrue(state is UiState.Success)
        assertEquals(ProfileInfo("ana", "ADMIN"), (state as UiState.Success).data)
    }

    private fun createViewModel(fakeDao: FakeUserSessionDao): ProfileViewModel {
        val authRepository = AuthRepository(
            authApi = FakeAuthApi(),
            tokenManager = TokenManager(fakeDao),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return ProfileViewModel(authRepository)
    }
}



