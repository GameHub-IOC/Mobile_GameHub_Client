package ioc.andresgq.gamehubmobile.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.repository.ReservationRepository
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            UserRole.USER -> loadMyReservations()
            UserRole.ADMIN -> loadAdminReservations()
        }
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

