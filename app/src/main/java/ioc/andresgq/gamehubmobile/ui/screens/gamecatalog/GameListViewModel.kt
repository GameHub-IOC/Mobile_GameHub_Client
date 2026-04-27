package ioc.andresgq.gamehubmobile.ui.screens.gamecatalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.remote.dto.GameDto
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modelo de UI de un juego para la capa de presentación.
 *
 * Desacopla la UI del [GameDto] de red, aplanando el objeto
 * anidado de categoría en un simple String.
 */
data class GameItemUi(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val numJugadores: String,
    val disponible: Boolean,
    val descripcion: String?,
    val observaciones: String?,
    val rutaImagen: String?
)

/**
 * ViewModel responsable exclusivamente del catálogo de juegos.
 *
 * Carga la lista de juegos desde [GameRepository] al iniciarse y expone
 * el estado resultante como [UiState]. Permite forzar una recarga con [loadCatalog].
 */
class CatalogViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _catalogState = MutableStateFlow<UiState<List<GameItemUi>>>(UiState.Idle)
    val catalogState: StateFlow<UiState<List<GameItemUi>>> = _catalogState.asStateFlow()

    init {
        loadCatalog()
    }

    /**
     * Carga el catálogo de juegos desde el repositorio.
     *
     * Si el estado actual es [UiState.Loading] o [UiState.Success], no hace nada.
     *
     * @param force Si se debe forzar la recarga, ignorando el estado actual.
     *              Por defecto es `false`.
     *              Si se establece en `true`, se ignorará el estado actual
     *              y se hará la recarga.
     */
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

    /**
     * Convierte el DTO de red en el modelo de UI compartido con el listado.
     *
     * @return El modelo de UI correspondiente al juego.
     */
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

/**
 * Factory para crear instancias de [CatalogViewModel].
 *
 * @param gameRepository repositorio del catálogo de juegos.
 */
class CatalogViewModelFactory(
    private val gameRepository: GameRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatalogViewModel(gameRepository) as T
}

