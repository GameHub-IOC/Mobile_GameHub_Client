package ioc.andresgq.gamehubmobile.ui.screens.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.ui.screens.login.LoginViewModel
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<UiState<UserSession>>(UiState.Idle)
    val uiState: StateFlow<UiState<UserSession>> = _uiState.asStateFlow()

    fun onUsernameChange(v: String) {
        username = v
    }

    fun onPasswordChange(v: String) {
        password = v
    }

    fun onConfirmPasswordChange(v: String) {
        confirmPassword = v
    }

    fun onEmailChange(v: String) {
        email = v
    }

    fun register() {
        // Validaciones locales antes de tocar la red
        when {
            username.isBlank() ->
                _uiState.value = UiState.Error("El nombre de usuario es obligatorio")

            email.isBlank() ->
                _uiState.value = UiState.Error("El email es obligatorio")

            !email.contains('@') ->
                _uiState.value = UiState.Error("El email no tiene un formato válido")

            password.isBlank() ->
                _uiState.value = UiState.Error("La contraseña es obligatoria")

            password.length < 6 ->
                _uiState.value = UiState.Error("La contraseña debe tener al menos 6 caracteres")

            password != confirmPassword ->
                _uiState.value = UiState.Error("Las contraseñas no coinciden")

            else -> viewModelScope.launch {
                _uiState.value = UiState.Loading
                val result = authRepository.register(
                    username.trim(),
                    password,
                    email.trim()
                )
                _uiState.value = result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "No se pudo registrar") }
                )
            }
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) _uiState.value = UiState.Idle
    }
}

class RegisterViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    /**
     * Crea una instancia del ViewModel solicitado.
     *
     * En este caso, siempre devuelve una instancia de [RegisterViewModel].
     *
     * @param modelClass clase del ViewModel que se desea crear.
     * @return una instancia de [RegisterViewModel] convertida al tipo genérico esperado.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(authRepository) as T
    }
}