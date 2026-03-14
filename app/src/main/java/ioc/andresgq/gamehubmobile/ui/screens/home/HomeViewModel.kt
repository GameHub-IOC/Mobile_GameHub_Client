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

data class HomeInfo(
    val username: String,
    val userType: String
)

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()

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

    suspend fun getCurrentUser(): HomeInfo? {
        return authRepository.getSession()?.let {
            HomeInfo(
                username = it.username,
                userType = it.userType
            )
        }
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(authRepository) as T
    }
}
