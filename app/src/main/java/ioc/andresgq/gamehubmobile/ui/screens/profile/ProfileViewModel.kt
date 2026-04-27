@file:Suppress("unused")

package ioc.andresgq.gamehubmobile.ui.screens.profile

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
 * Información de perfil mínima necesaria para la navegación principal.
 */
data class ProfileInfo(
    val username: String,
    val userType: String
)

/**
 * ViewModel del área de perfil/sesión.
 *
 * Se encarga de exponer la sesión activa y de cerrar sesión cuando se solicite.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<ProfileInfo>>(UiState.Loading)
    val profileState: StateFlow<UiState<ProfileInfo>> = _profileState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Carga la información del perfil actual.
     */
    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            val session = authRepository.getSession()
            _profileState.value = if (session == null) {
                UiState.Error("No hay ninguna sesión activa")
            } else {
                UiState.Success(
                    ProfileInfo(
                        username = session.username,
                        userType = session.userType
                    )
                )
            }
        }
    }

    /**
     * Cierra la sesión actual.
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
     * Consume el estado de logout.
     */
    fun consumeLogoutState() {
        _logoutState.value = UiState.Idle
    }
}

/**
 * Fábrica de ViewModel para el área de perfil/sesión.
 */
class ProfileViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(authRepository) as T
    }
}

