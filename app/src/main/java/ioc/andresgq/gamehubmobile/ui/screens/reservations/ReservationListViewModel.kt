package ioc.andresgq.gamehubmobile.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.repository.ReservationRepository
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la lista de reservas.
 */
class ReservationListViewModel(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _myReservationsState = MutableStateFlow<UiState<List<ReservationListItemUi>>>(UiState.Loading)
    val myReservationsState: StateFlow<UiState<List<ReservationListItemUi>>> = _myReservationsState.asStateFlow()

    private val _adminReservationsState = MutableStateFlow<UiState<List<ReservationListItemUi>>>(UiState.Loading)
    val adminReservationsState: StateFlow<UiState<List<ReservationListItemUi>>> = _adminReservationsState.asStateFlow()

    // ── Filtros admin ──────────────────────────────────────────────────────────

    /** null = mostrar todos los estados; valor = filtrar por ese estado exacto. */
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    /** Texto libre para filtrar por nombre de usuario (vacío = sin filtro). */
    private val _userFilter = MutableStateFlow("")
    val userFilter: StateFlow<String> = _userFilter.asStateFlow()

    /**
     * Lista filtrada de reservas globales, derivada de [_adminReservationsState]
     * combinada con los filtros activos.
     */
    val filteredAdminReservationsState: StateFlow<UiState<List<ReservationListItemUi>>> =
        combine(_adminReservationsState, _statusFilter, _userFilter) { state, status, user ->
            when (state) {
                is UiState.Success -> {
                    val filtered = state.data.filter { item ->
                        val matchStatus = status == null ||
                                item.estado.uppercase() == status.uppercase()
                        val matchUser = user.isBlank() ||
                                item.usuarioNombre.contains(user.trim(), ignoreCase = true)
                        matchStatus && matchUser
                    }
                    UiState.Success(filtered)
                }
                else -> state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    // ── Acción de borrado ──────────────────────────────────────────────────────

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    /**
     * Carga las reservas del usuario actual.
     */
    fun loadMyReservations() {
        viewModelScope.launch {
            _myReservationsState.value = UiState.Loading
            val result = reservationRepository.getMyReservations()
            _myReservationsState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "No se pudieron cargar tus reservas") }
            )
        }
    }

    /**
     * Carga las reservas globales.
     */
    fun loadAdminReservations() {
        viewModelScope.launch {
            _adminReservationsState.value = UiState.Loading
            val result = reservationRepository.getAdminReservations()
            _adminReservationsState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "No se pudieron cargar las reservas") }
            )
        }
    }

    /**
     * Refresca las reservas según el rol actual.
     */
    fun refreshForRole(role: UserRole) {
        when (role) {
            UserRole.USER  -> loadMyReservations()
            UserRole.ADMIN -> loadAdminReservations()
        }
    }

    // ── Filtros ────────────────────────────────────────────────────────────────

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
    }

    fun setUserFilter(user: String) {
        _userFilter.value = user
    }

    // ── Borrado ────────────────────────────────────────────────────────────────

    /**
     * Cancela/elimina la reserva con el [id] dado y recarga la lista del [role].
     */
    fun deleteReservation(id: Long, role: UserRole) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            val result = reservationRepository.deleteReservation(id)
            _deleteState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "No se pudo cancelar la reserva") }
            )
            if (result.isSuccess) {
                refreshForRole(role)
            }
        }
    }

    /** Consume el estado del último borrado, volviendo a [UiState.Idle]. */
    fun consumeDeleteState() {
        _deleteState.value = UiState.Idle
    }
}

/**
 * Fábrica de ViewModel para la lista de reservas.
 */
class ReservationListViewModelFactory(
    private val reservationRepository: ReservationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReservationListViewModel(reservationRepository) as T
    }
}
