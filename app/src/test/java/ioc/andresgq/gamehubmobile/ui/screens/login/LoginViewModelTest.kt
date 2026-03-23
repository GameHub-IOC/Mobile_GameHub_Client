package ioc.andresgq.gamehubmobile.ui.screens.login

import ioc.andresgq.gamehubmobile.data.local.TokenManager
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_withBlankFields_setsValidationError() {
        val fakeAuthApi = FakeAuthApi()
        val viewModel = createViewModel(fakeAuthApi)

        viewModel.login()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("obligatorios"))
        assertEquals(null, fakeAuthApi.lastLoginRequest)
    }

    @Test
    fun login_success_trimsUsernameAndPublishesSuccess() = runTest(mainDispatcherRule.scheduler) {
        val fakeAuthApi = FakeAuthApi()
        fakeAuthApi.loginResponse = fakeAuthApi.loginResponse.copy(nombre = "pepe")
        val viewModel = createViewModel(fakeAuthApi)

        viewModel.onUsernameChange("  pepe  ")
        viewModel.onPasswordChange("123456")
        viewModel.login()
        advanceUntilIdle()

        assertEquals("pepe", fakeAuthApi.lastLoginRequest?.nombre)
        assertEquals("123456", fakeAuthApi.lastLoginRequest?.password)
        assertTrue(viewModel.uiState.value is UiState.Success)
    }

    @Test
    fun clearError_fromErrorState_returnsToIdle() {
        val viewModel = createViewModel(FakeAuthApi())

        viewModel.login()
        viewModel.clearError()

        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    private fun createViewModel(fakeAuthApi: FakeAuthApi): LoginViewModel {
        val repository = AuthRepository(
            authApi = fakeAuthApi,
            tokenManager = TokenManager(FakeUserSessionDao()),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return LoginViewModel(repository)
    }
}

