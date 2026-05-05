package ioc.andresgq.gamehubmobile.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoriaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameRequestDto
import ioc.andresgq.gamehubmobile.data.repository.CategoriaRepository
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameItemUi
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de gestión de juegos (CRUD admin).
 *
 * Expone:
 * - [gamesState]: lista de juegos del catálogo.
 * - [categoriasState]: lista de categorías disponibles (para el formulario).
 * - [operationState]: estado de la última operación de escritura (crear/editar/eliminar).
 */
class GameManagementViewModel(
    private val gameRepository: GameRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _gamesState = MutableStateFlow<UiState<List<GameItemUi>>>(UiState.Idle)
    val gamesState: StateFlow<UiState<List<GameItemUi>>> = _gamesState.asStateFlow()

    private val _categoriasState = MutableStateFlow<UiState<List<CategoriaDto>>>(UiState.Idle)
    val categoriasState: StateFlow<UiState<List<CategoriaDto>>> = _categoriasState.asStateFlow()

    /** Estado de la última operación de escritura (crear / actualizar / eliminar). */
    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    init {
        loadGames()
        loadCategorias()
    }

    /** Carga o recarga la lista de juegos desde el repositorio. */
    fun loadGames(force: Boolean = false) {
        if (!force && (_gamesState.value is UiState.Loading || _gamesState.value is UiState.Success)) return
        viewModelScope.launch {
            _gamesState.value = UiState.Loading
            _gamesState.value = gameRepository.getGames().fold(
                onSuccess = { list ->
                    UiState.Success(list.map { dto ->
                        GameItemUi(
                            id = dto.id,
                            nombre = dto.nombre,
                            categoria = dto.categoria.nombre,
                            numJugadores = dto.numJugadores,
                            disponible = dto.disponible,
                            descripcion = dto.descripcion,
                            observaciones = dto.observaciones,
                            rutaImagen = dto.rutaImagen
                        )
                    })
                },
                onFailure = { UiState.Error(it.message ?: "Error al cargar juegos") }
            )
        }
    }

    /** Carga el listado de categorías (necesario para el desplegable del formulario). */
    fun loadCategorias() {
        if (_categoriasState.value is UiState.Loading || _categoriasState.value is UiState.Success) return
        viewModelScope.launch {
            _categoriasState.value = UiState.Loading
            _categoriasState.value = categoriaRepository.getCategorias().fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar categorías") }
            )
        }
    }

    /**
     * Crea un nuevo juego en el catálogo.
     *
     * @param request datos del formulario de alta.
     */
    fun createGame(request: GameRequestDto) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = gameRepository.createGame(request).fold(
                onSuccess = {
                    loadGames(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al crear el juego") }
            )
        }
    }

    /**
     * Actualiza un juego existente.
     *
     * @param id      id del juego a modificar.
     * @param request nuevos datos del formulario.
     */
    fun updateGame(id: Long, request: GameRequestDto) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = gameRepository.updateGame(id, request).fold(
                onSuccess = {
                    loadGames(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar el juego") }
            )
        }
    }

    /**
     * Elimina un juego del catálogo.
     *
     * @param id id del juego a eliminar.
     */
    fun deleteGame(id: Long) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = gameRepository.deleteGame(id).fold(
                onSuccess = {
                    loadGames(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar el juego") }
            )
        }
    }

    /** Resetea el estado de operación a [UiState.Idle] tras consumir el feedback de UI. */
    fun consumeOperationState() {
        _operationState.value = UiState.Idle
    }
}

/**
 * Factory para crear instancias de [GameManagementViewModel].
 *
 * @param gameRepository       repositorio del catálogo de juegos.
 * @param categoriaRepository  repositorio de categorías.
 */
class GameManagementViewModelFactory(
    private val gameRepository: GameRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GameManagementViewModel(gameRepository, categoriaRepository) as T
}

