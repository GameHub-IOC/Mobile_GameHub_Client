package ioc.andresgq.gamehubmobile.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.repository.UserRepository
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  Modelo de presentación
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Modelo de usuario adaptado a la capa de UI.
 *
 * @property id     identificador único del usuario.
 * @property nombre nombre de acceso.
 * @property rol    rol actual: "ADMIN" o "USER".
 */
data class UserItemUi(
    val id: Long,
    val nombre: String,
    val rol: String
)

// ─────────────────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel para la pantalla de gestión de usuarios (exclusiva para ADMIN).
 *
 * Expone:
 * - [usersState]: lista de usuarios del sistema.
 * - [operationState]: estado de la última operación de escritura (cambio de rol / eliminación).
 *
 * @property userRepository repositorio que gestiona los endpoints `/usuarios`.
 */
class UsersViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _usersState = MutableStateFlow<UiState<List<UserItemUi>>>(UiState.Idle)
    val usersState: StateFlow<UiState<List<UserItemUi>>> = _usersState.asStateFlow()

    /** Estado de la última operación de escritura (cambio de rol / eliminar). */
    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    init {
        loadUsers()
    }

    // ── Lectura ──────────────────────────────────────────────────────────────

    /**
     * Carga o recarga el listado de usuarios desde el repositorio.
     *
     * @param force `true` para forzar recarga aunque los datos ya estén en memoria.
     */
    fun loadUsers(force: Boolean = false) {
        if (!force && (_usersState.value is UiState.Loading || _usersState.value is UiState.Success)) return
        viewModelScope.launch {
            _usersState.value = UiState.Loading
            _usersState.value = userRepository.getUsers().fold(
                onSuccess = { list ->
                    val uiList = list.map { dto ->
                        UserItemUi(id = dto.id, nombre = dto.nombre, rol = dto.rol)
                    }.sortedWith(
                        compareByDescending<UserItemUi> { it.rol == "ADMIN" }
                            .thenBy { it.nombre.lowercase() }
                    )
                    UiState.Success(uiList)
                },
                onFailure = { UiState.Error(it.message ?: "Error al cargar usuarios") }
            )
        }
    }

    // ── Escritura ────────────────────────────────────────────────────────────

    /**
     * Alterna el rol del usuario entre "USER" y "ADMIN".
     *
     * Tras completarse, recarga el listado para reflejar el cambio.
     *
     * @param user usuario cuyo rol se quiere cambiar.
     */
    fun toggleRole(user: UserItemUi) {
        val newRole = if (user.rol == "ADMIN") "USER" else "ADMIN"
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = userRepository.changeRole(user.id, user.nombre, newRole).fold(
                onSuccess = { loadUsers(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al cambiar el rol") }
            )
        }
    }

    /**
     * Elimina permanentemente un usuario del sistema.
     *
     * @param id identificador del usuario a eliminar.
     */
    fun deleteUser(id: Long) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = userRepository.deleteUser(id).fold(
                onSuccess = { loadUsers(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al eliminar el usuario") }
            )
        }
    }

    /**
     * Crea un nuevo usuario con los datos proporcionados.
     *
     * Tras completarse con éxito recarga el listado para mostrar el nuevo usuario.
     *
     * @param nombre   nombre de acceso único.
     * @param password contraseña en texto plano.
     * @param rol      rol inicial: `"ADMIN"` o `"USER"`.
     */
    fun createUser(nombre: String, password: String, rol: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            _operationState.value = userRepository.createUser(nombre, password, rol).fold(
                onSuccess = { loadUsers(force = true); UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error al crear el usuario") }
            )
        }
    }

    /** Resetea el estado de operación a [UiState.Idle] tras consumir el feedback de la UI. */
    fun consumeOperationState() {
        _operationState.value = UiState.Idle
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Factory
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Factory para crear instancias de [UsersViewModel] con el repositorio de usuarios.
 */
class UsersViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        UsersViewModel(userRepository) as T
}


