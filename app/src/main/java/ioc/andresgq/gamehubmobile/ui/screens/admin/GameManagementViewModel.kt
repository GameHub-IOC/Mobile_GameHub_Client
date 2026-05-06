package ioc.andresgq.gamehubmobile.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoryDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.repository.CategoryRepository
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.data.repository.TableRepository
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
    private val categoryRepository: CategoryRepository,
    private val tableRepository: TableRepository
) : ViewModel() {

    private val _gamesState = MutableStateFlow<UiState<List<GameItemUi>>>(UiState.Idle)
    val gamesState: StateFlow<UiState<List<GameItemUi>>> = _gamesState.asStateFlow()

    private val _categoriasState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Idle)
    val categoriasState: StateFlow<UiState<List<CategoryDto>>> = _categoriasState.asStateFlow()

    private val _mesasState = MutableStateFlow<UiState<List<ReservationMesaOperativaDto>>>(UiState.Idle)
    val mesasState: StateFlow<UiState<List<ReservationMesaOperativaDto>>> = _mesasState.asStateFlow()

    /** Estado de la última operación de escritura (crear / actualizar / eliminar). */
    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    init {
        loadGames()
        loadCategorias(force = false)
        loadMesas(force = false)
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
    fun loadCategorias(force: Boolean = false) {
        if (!force && (_categoriasState.value is UiState.Loading || _categoriasState.value is UiState.Success)) return
        viewModelScope.launch {
            _categoriasState.value = UiState.Loading
            _categoriasState.value = categoryRepository.getCategorias().fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar categorías") }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CRUD de mesas
    // ─────────────────────────────────────────────────────────────────────────

    /** Carga o recarga el listado de mesas desde el repositorio. */
    fun loadMesas(force: Boolean = false) {
        if (!force && (_mesasState.value is UiState.Loading || _mesasState.value is UiState.Success)) return
        viewModelScope.launch {
            _mesasState.value = UiState.Loading
            _mesasState.value = tableRepository.getMesas().fold(
                onSuccess = { UiState.Success(it.sortedBy { m -> m.numero }) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar mesas") }
            )
        }
    }

    /** Crea una nueva mesa. */
    fun createMesa(numero: Int, capacidad: Int) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = tableRepository.createMesa(numero, capacidad).fold(
                onSuccess = {
                    loadMesas(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al crear la mesa") }
            )
        }
    }

    /** Actualiza los datos de una mesa existente. */
    fun updateMesa(id: Long, numero: Int, capacidad: Int, operativa: Boolean) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = tableRepository.updateMesa(id, numero, capacidad, operativa).fold(
                onSuccess = {
                    loadMesas(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar la mesa") }
            )
        }
    }

    /** Elimina una mesa por su id. */
    fun deleteMesa(id: Long) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = tableRepository.deleteMesa(id).fold(
                onSuccess = {
                    loadMesas(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar la mesa") }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CRUD de categorías
    // ─────────────────────────────────────────────────────────────────────────

    /** Crea una nueva categoría. */
    fun createCategoria(nombre: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = categoryRepository.createCategoria(nombre).fold(
                onSuccess = {
                    loadCategorias(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al crear la categoría") }
            )
        }
    }

    /** Actualiza el nombre de una categoría existente. */
    fun updateCategoria(id: Long, nuevoNombre: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = categoryRepository.updateCategoria(id, nuevoNombre).fold(
                onSuccess = {
                    loadCategorias(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar la categoría") }
            )
        }
    }

    /** Elimina una categoría por su nombre. */
    fun deleteCategoria(nombre: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = categoryRepository.deleteCategoria(nombre).fold(
                onSuccess = {
                    loadCategorias(force = true)
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar la categoría") }
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
 * @param categoryRepository  repositorio de categorías.
 * @param tableRepository       repositorio de mesas del local.
 */
class GameManagementViewModelFactory(
    private val gameRepository: GameRepository,
    private val categoryRepository: CategoryRepository,
    private val tableRepository: TableRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GameManagementViewModel(gameRepository, categoryRepository, tableRepository) as T
}

