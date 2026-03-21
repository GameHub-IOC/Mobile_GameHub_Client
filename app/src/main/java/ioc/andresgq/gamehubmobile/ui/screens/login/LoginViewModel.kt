package ioc.andresgq.gamehubmobile.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar el estado de la pantalla de login.
 *
 * Esta clase mantiene los valores introducidos por el usuario (`username` y `password`)
 * y expone un estado observable `uiState` para que la interfaz pueda reaccionar
 * al proceso de autenticación.
 *
 * También delega la lógica de login en [AuthRepository], transformando su resultado
 * en estados de UI como carga, éxito o error.
 *
 * @property authRepository repositorio que ejecuta la autenticación y gestiona la sesión.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Nombre de usuario introducido en el formulario.
     *
     * Se expone solo para lectura desde fuera del ViewModel; los cambios deben
     * realizarse mediante [onUsernameChange].
     */
    var username by mutableStateOf("")
        private set

    /**
     * Contraseña introducida en el formulario.
     *
     * Se expone solo para lectura desde fuera del ViewModel; los cambios deben
     * realizarse mediante [onPasswordChange].
     */
    var password by mutableStateOf("")
        private set

    /**
     * Estado interno mutable del proceso de login.
     *
     * Comienza en [UiState.Idle] y se actualiza a [UiState.Loading],
     * [UiState.Success] o [UiState.Error] según el resultado.
     */
    private val _uiState = MutableStateFlow<UiState<UserSession>>(UiState.Idle)

    /**
     * Estado público e inmutable observado por la interfaz.
     *
     * La UI puede suscribirse a este flujo para reaccionar a cambios durante
     * la autenticación del usuario.
     */
    val uiState: StateFlow<UiState<UserSession>> = _uiState.asStateFlow()

    /**
     * Actualiza el nombre de usuario escrito por el usuario.
     *
     * @param value nuevo valor del campo de nombre de usuario.
     */
    fun onUsernameChange(value: String) {
        username = value
    }

    /**
     * Actualiza la contraseña escrita por el usuario.
     *
     * @param value nuevo valor del campo de contraseña.
     */
    fun onPasswordChange(value: String) {
        password = value
    }

    /**
     * Inicia el proceso de autenticación.
     *
     * Primero valida localmente que ambos campos tengan contenido. Si alguno está vacío,
     * publica un estado de error:
     *
     * ```kotlin
     * if (username.isBlank() || password.isBlank()) {
     *     _uiState.value = UiState.Error("Usuario y contraseña son obligatorios")
     * }
     * ```
     *
     * Si la validación pasa, lanza una corrutina en [viewModelScope], marca el estado
     * como cargando y delega el login en [authRepository]. Después transforma el resultado
     * en un estado de éxito o error:
     *
     * ```kotlin
     * _uiState.value = result.fold(
     *     onSuccess = { UiState.Success(it) },
     *     onFailure = { UiState.Error(it.message ?: "No se pudo iniciar sesión") }
     * )
     * ```
     */
    fun login() {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Usuario y contraseña son obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.login(username.trim(), password)
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "No se pudo iniciar sesión") }
            )
        }
    }

    /**
     * Limpia el estado de error actual, si existe.
     *
     * Este método resulta útil cuando la UI quiere ocultar un mensaje de error
     * después de mostrarlo al usuario o cuando este vuelve a interactuar con el formulario.
     */
    fun clearError() {
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Idle
        }
    }
}

/**
 * Factoría para crear instancias de [LoginViewModel] con dependencias manuales.
 *
 * Como [LoginViewModel] necesita un [AuthRepository] en el constructor, se utiliza
 * esta factoría para integrarlo con el sistema de creación de ViewModels de Android.
 *
 * @property authRepository repositorio que será inyectado en el ViewModel creado.
 */
class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    /**
     * Crea una instancia del ViewModel solicitado.
     *
     * En este caso, siempre devuelve una instancia de [LoginViewModel].
     *
     * @param modelClass clase del ViewModel que se desea crear.
     * @return una instancia de [LoginViewModel] convertida al tipo genérico esperado.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(authRepository) as T
    }
}