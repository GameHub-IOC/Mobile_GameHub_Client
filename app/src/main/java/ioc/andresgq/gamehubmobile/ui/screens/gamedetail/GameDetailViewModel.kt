package ioc.andresgq.gamehubmobile.ui.screens.gamedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameItemUi
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de un juego.
 *
 * Recibe el [gameId] al construirse y carga el juego desde [gameRepository]
 * aplicando la misma estrategia network-first con fallback a caché que el resto
 * del catálogo.
 *
 * @param gameRepository repositorio del catálogo de juegos.
 * @param gameId         identificador del juego a mostrar.
 */
class GameDetailViewModel(
    private val gameRepository: GameRepository,
    private val gameId: Long
) : ViewModel() {

    private val _gameState = MutableStateFlow<UiState<GameItemUi>>(UiState.Loading)
    val gameState: StateFlow<UiState<GameItemUi>> = _gameState.asStateFlow()

    init {
        loadGame()
    }

    /** Carga (o recarga) el juego desde el repositorio. */
    fun loadGame() {
        viewModelScope.launch {
            _gameState.value = UiState.Loading
            val result = gameRepository.getGameById(gameId)
            _gameState.value = result.fold(
                onSuccess = { game -> UiState.Success(game.toUi()) },
                onFailure = { error ->
                    UiState.Error(error.message ?: "No se pudo cargar el juego")
                }
            )
        }
    }

    /** Convierte el DTO de red en el modelo de UI compartido con el listado. */
    private fun GameDto.toUi(): GameItemUi = GameItemUi(
        id = id,
        nombre = nombre,
        categoria = categoria.nombre,
        numJugadores = numJugadores,
        disponible = disponible,
        descripcion = descripcion,
        observaciones = observaciones,
        rutaImagen = rutaImagen
    )
}

class GameDetailViewModelFactory(
    private val gameRepository: GameRepository,
    private val gameId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GameDetailViewModel(gameRepository, gameId) as T
}