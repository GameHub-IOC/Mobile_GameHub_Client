package ioc.andresgq.gamehubmobile.ui.screens.register

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
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun register_invalidEmail_setsValidationError() {
        val viewModel = createViewModel(FakeAuthApi())

        viewModel.onUsernameChange("usuario")
        viewModel.onEmailChange("correo-invalido")
        viewModel.onPasswordChange("123456")
        viewModel.onConfirmPasswordChange("123456")
        viewModel.register()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("formato"))
    }

    @Test
    fun register_passwordMismatch_setsValidationError() {
        val viewModel = createViewModel(FakeAuthApi())

        viewModel.onUsernameChange("usuario")
        viewModel.onEmailChange("user@mail.com")
        viewModel.onPasswordChange("123456")
        viewModel.onConfirmPasswordChange("abcdef")
        viewModel.register()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("no coinciden"))
    }

    @Test
    fun register_success_trimsInputAndPublishesSuccess() = runTest(mainDispatcherRule.scheduler) {
        val fakeAuthApi = FakeAuthApi()
        fakeAuthApi.registerResponse = fakeAuthApi.registerResponse.copy(nombre = "user")
        val viewModel = createViewModel(fakeAuthApi)

        viewModel.onUsernameChange("  user  ")
        viewModel.onEmailChange("  user@mail.com  ")
        viewModel.onPasswordChange("123456")
        viewModel.onConfirmPasswordChange("123456")
        viewModel.register()
        advanceUntilIdle()

        assertEquals("user", fakeAuthApi.lastRegisterRequest?.nombre)
        assertEquals("user@mail.com", fakeAuthApi.lastRegisterRequest?.email)
        assertTrue(viewModel.uiState.value is UiState.Success)
    }

    @Test
    fun clearError_fromErrorState_returnsToIdle() {
        val viewModel = createViewModel(FakeAuthApi())

        viewModel.register()
        viewModel.clearError()

        assertEquals(UiState.Idle, viewModel.uiState.value)
    }

    private fun createViewModel(fakeAuthApi: FakeAuthApi): RegisterViewModel {
        val repository = AuthRepository(
            authApi = fakeAuthApi,
            tokenManager = TokenManager(FakeUserSessionDao()),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return RegisterViewModel(repository)
    }
}


