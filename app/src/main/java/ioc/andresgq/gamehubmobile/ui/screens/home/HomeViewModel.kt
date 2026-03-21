package ioc.andresgq.gamehubmobile.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modelo simple con la información que la pantalla Home necesita mostrar
 * sobre el usuario autenticado.
 *
 * @property username nombre del usuario actual.
 * @property userType tipo o rol del usuario actual.
 */
data class HomeInfo(
    val username: String,
    val userType: String
)

/**
 * ViewModel de la pantalla principal (`Home`).
 *
 * Se encarga de exponer el estado del proceso de cierre de sesión y de
 * recuperar la información del usuario autenticado desde el repositorio.
 *
 * @property authRepository repositorio que centraliza la lógica de sesión
 * y autenticación.
 */
class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Estado interno mutable del proceso de logout.
     *
     * Empieza en [UiState.Idle], pasa a [UiState.Loading] mientras se ejecuta
     * la operación, y finalmente a [UiState.Success] o [UiState.Error].
     */
    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)

    /**
     * Estado público e inmutable del cierre de sesión observado por la UI.
     */
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()

    /**
     * Cierra la sesión del usuario actual.
     *
     * Lanza una corrutina en [viewModelScope] para ejecutar la operación de forma
     * segura respecto al ciclo de vida. Actualiza [logoutState] para que la interfaz
     * pueda reaccionar al progreso y al resultado del proceso.
     */
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading
            try {
                authRepository.logout()
                _logoutState.value = UiState.Success(Unit)
            } catch (_: Exception) {
                _logoutState.value = UiState.Error("No se pudo cerrar sesión")
            }
        }
    }

    /**
     * Recupera la información básica del usuario autenticado.
     *
     * Consulta la sesión actual en [authRepository] y, si existe, la transforma
     * en un objeto [HomeInfo] más adecuado para la capa de presentación.
     *
     * @return un [HomeInfo] con el nombre y tipo de usuario, o `null` si no hay
     * una sesión activa.
     */
    suspend fun getCurrentUser(): HomeInfo? {
        return authRepository.getSession()?.let {
            HomeInfo(
                username = it.username,
                userType = it.userType
            )
        }
    }
}

/**
 * Factoría para crear instancias de [HomeViewModel] con dependencias manuales.
 *
 * Se utiliza porque [HomeViewModel] necesita un [AuthRepository] en el
 * constructor y no puede ser creado directamente por el proveedor por defecto.
 *
 * @property authRepository repositorio que se inyectará en el ViewModel creado.
 */
class HomeViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    /**
     * Crea una nueva instancia del ViewModel solicitado.
     *
     * @param modelClass clase del ViewModel que se desea crear.
     * @return una instancia de [HomeViewModel].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(authRepository) as T
    }
}