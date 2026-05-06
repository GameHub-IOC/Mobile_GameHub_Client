package ioc.andresgq.gamehubmobile.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoryDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import ioc.andresgq.gamehubmobile.data.repository.CategoryRepository
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.data.repository.TableRepository
import ioc.andresgq.gamehubmobile.data.repository.TurnRepository
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameItemUi
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de gestión de administrador.
 *
 * Expone:
 * - [gamesState]: lista de juegos del catálogo.
 * - [categoriasState]: lista de categorías (también usada por el formulario de juegos).
 * - [mesasState]: lista de mesas del local.
 * - [turnosState]: lista de turnos horarios.
 * - [operationState]: estado de la última operación de escritura.
 */
class GameManagementViewModel(
    private val gameRepository: GameRepository,
    private val categoryRepository: CategoryRepository,
    private val tableRepository: TableRepository,
    private val turnRepository: TurnRepository
) : ViewModel() {

    private val _gamesState = MutableStateFlow<UiState<List<GameItemUi>>>(UiState.Idle)
    val gamesState: StateFlow<UiState<List<GameItemUi>>> = _gamesState.asStateFlow()

    private val _categoriasState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Idle)
    val categoriasState: StateFlow<UiState<List<CategoryDto>>> = _categoriasState.asStateFlow()

    private val _mesasState = MutableStateFlow<UiState<List<ReservationMesaOperativaDto>>>(UiState.Idle)
    val mesasState: StateFlow<UiState<List<ReservationMesaOperativaDto>>> = _mesasState.asStateFlow()

    private val _turnosState = MutableStateFlow<UiState<List<ReservationTurnoDto>>>(UiState.Idle)
    val turnosState: StateFlow<UiState<List<ReservationTurnoDto>>> = _turnosState.asStateFlow()

    /** Estado de la última operación de escritura (crear / actualizar / eliminar). */
    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    init {
        loadGames()
        loadCategorias(force = false)
        loadMesas(force = false)
        loadTurnos(force = false)
    }

    // ── Juegos ───────────────────────────────────────────────────────────────

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

    fun createGame(request: GameRequestDto) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = gameRepository.createGame(request).fold(
                onSuccess = { loadGames(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al crear el juego") }
            )
        }
    }

    fun updateGame(id: Long, request: GameRequestDto) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = gameRepository.updateGame(id, request).fold(
                onSuccess = { loadGames(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar el juego") }
            )
        }
    }

    fun deleteGame(id: Long) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = gameRepository.deleteGame(id).fold(
                onSuccess = { loadGames(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar el juego") }
            )
        }
    }

    // ── Categorías ────────────────────────────────────────────────────────────

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

    fun createCategoria(nombre: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = categoryRepository.createCategoria(nombre).fold(
                onSuccess = { loadCategorias(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al crear la categoría") }
            )
        }
    }

    fun updateCategoria(id: Long, nuevoNombre: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = categoryRepository.updateCategoria(id, nuevoNombre).fold(
                onSuccess = { loadCategorias(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar la categoría") }
            )
        }
    }

    fun deleteCategoria(nombre: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = categoryRepository.deleteCategoria(nombre).fold(
                onSuccess = { loadCategorias(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar la categoría") }
            )
        }
    }

    // ── Mesas ─────────────────────────────────────────────────────────────────

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

    fun createMesa(numero: Int, capacidad: Int) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = tableRepository.createMesa(numero, capacidad).fold(
                onSuccess = { loadMesas(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al crear la mesa") }
            )
        }
    }

    fun updateMesa(id: Long, numero: Int, capacidad: Int, operativa: Boolean) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = tableRepository.updateMesa(id, numero, capacidad, operativa).fold(
                onSuccess = { loadMesas(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar la mesa") }
            )
        }
    }

    fun deleteMesa(id: Long) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = tableRepository.deleteMesa(id).fold(
                onSuccess = { loadMesas(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar la mesa") }
            )
        }
    }

    // ── Turnos ────────────────────────────────────────────────────────────────

    /** Carga o recarga el listado de turnos desde el repositorio. */
    fun loadTurnos(force: Boolean = false) {
        if (!force && (_turnosState.value is UiState.Loading || _turnosState.value is UiState.Success)) return
        viewModelScope.launch {
            _turnosState.value = UiState.Loading
            _turnosState.value = turnRepository.getTurnos().fold(
                onSuccess = { UiState.Success(it.sortedBy { t -> t.id }) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar turnos") }
            )
        }
    }

    fun createTurno(nombre: String, horaInicio: String?, horaFin: String?) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = turnRepository.createTurno(nombre, horaInicio, horaFin).fold(
                onSuccess = { loadTurnos(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al crear el turno") }
            )
        }
    }

    fun updateTurno(id: Long, nombre: String, horaInicio: String?, horaFin: String?) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = turnRepository.updateTurno(id, nombre, horaInicio, horaFin).fold(
                onSuccess = { loadTurnos(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar el turno") }
            )
        }
    }

    fun deleteTurno(id: Long) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = turnRepository.deleteTurno(id).fold(
                onSuccess = { loadTurnos(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar el turno") }
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
 */
class GameManagementViewModelFactory(
    private val gameRepository: GameRepository,
    private val categoryRepository: CategoryRepository,
    private val tableRepository: TableRepository,
    private val turnRepository: TurnRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GameManagementViewModel(gameRepository, categoryRepository, tableRepository, turnRepository) as T
}

