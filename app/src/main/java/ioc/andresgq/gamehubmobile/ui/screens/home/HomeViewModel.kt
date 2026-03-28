package ioc.andresgq.gamehubmobile.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeInfo(
    val username: String,
    val userType: String
)

data class GameItemUi(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val numJugadores: String,
    val disponible: Boolean,
    val descripcion: String?,
    val rutaImagen: String?
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()

    private val _catalogState = MutableStateFlow<UiState<List<GameItemUi>>>(UiState.Idle)
    val catalogState: StateFlow<UiState<List<GameItemUi>>> = _catalogState.asStateFlow()

    init {
        loadCatalog()
    }

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

    fun loadCatalog(force: Boolean = false) {
        if (!force && (_catalogState.value is UiState.Loading || _catalogState.value is UiState.Success)) {
            return
        }

        viewModelScope.launch {
            _catalogState.value = UiState.Loading
            val result = gameRepository.getGames()

            _catalogState.value = result.fold(
                onSuccess = { games -> UiState.Success(games.map { it.toUi() }) },
                onFailure = { error ->
                    UiState.Error(error.message ?: "No se pudo cargar el catálogo")
                }
            )
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

    private fun GameDto.toUi(): GameItemUi {
        return GameItemUi(
            id = id,
            nombre = nombre,
            categoria = categoria.nombre,
            numJugadores = numJugadores,
            disponible = disponible,
            descripcion = descripcion,
            rutaImagen = rutaImagen
        )
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(authRepository, gameRepository) as T
    }
}