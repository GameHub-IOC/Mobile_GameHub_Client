package ioc.andresgq.gamehubmobile.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.data.repository.ReservationRepository
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.ui.screens.reservations.ReservationListItemUi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel del Dashboard.
 *
 * Ejecuta en paralelo cuatro llamadas al backend para construir el [DashboardUiState]:
 * - Reservas del usuario (o del admin) → próxima reserva + historial reciente.
 * - Turnos disponibles → contador de disponibilidad.
 * - Mesas operativas   → contador de disponibilidad.
 * - Juegos disponibles → contador de disponibilidad.
 *
 * Las llamadas fallidas no rompen el estado: se marcan como datos parciales.
 *
 * @param reservationRepository repositorio de reservas, turnos y mesas.
 * @param gameRepository        repositorio del catálogo de juegos.
 * @param role                  rol del usuario, determina si se usa mis-reservas o /reservas.
 */
class DashboardViewModel(
    private val reservationRepository: ReservationRepository,
    private val gameRepository: GameRepository,
    private val role: UserRole
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            role = role,
            currentDateLabel = buildDateLabel()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Actualiza el nombre de usuario en el estado.
     * Llamado desde la capa de UI cuando el ProfileViewModel entrega el nombre.
     */
    fun setUsername(name: String) {
        if (_uiState.value.username != name) {
            _uiState.value = _uiState.value.copy(username = name)
        }
    }

    /**
     * Recarga todos los datos del dashboard en paralelo.
     * Safe ante fallos parciales: cada fuente que falle deja su valor en 0/null
     * y activa [DashboardUiState.isPartialData].
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                currentDateLabel = buildDateLabel()
            )

            val today = todayString()

            supervisorScope {
                val reservationsDeferred = async {
                    if (role == UserRole.ADMIN) {
                        reservationRepository.getAdminReservations()
                    } else {
                        reservationRepository.getMyReservations()
                    }
                }
                val turnsDeferred = async { reservationRepository.getTurnOptions() }
                val tablesDeferred = async { reservationRepository.getOperationalTableOptions() }
                val gamesDeferred = async { gameRepository.getAvailableGames() }

                val reservationsResult = reservationsDeferred.await()
                val turnsResult = turnsDeferred.await()
                val tablesResult = tablesDeferred.await()
                val gamesResult = gamesDeferred.await()

                val isPartialData = listOf(reservationsResult, turnsResult, tablesResult, gamesResult)
                    .any { it.isFailure }

                val reservations = reservationsResult.getOrElse { emptyList() }
                val turns = turnsResult.getOrElse { emptyList() }
                val tables = tablesResult.getOrElse { emptyList() }
                val games = gamesResult.getOrElse { emptyList() }

                val nextReservation = computeNextReservation(reservations, today)
                val recentReservations = reservations.take(5)
                val todayCount = reservations.count { it.fecha == today }
                val pendingCount = reservations.count { it.estado.uppercase() == "PENDIENTE" }

                val alertMessage = buildAlertMessage(
                    today = today,
                    nextReservation = nextReservation,
                    availableTurnsCount = turns.size,
                    todayCount = todayCount,
                    pendingCount = pendingCount
                )

                _uiState.value = _uiState.value.copy(
                    nextReservation = nextReservation,
                    recentReservations = recentReservations,
                    todayReservationsCount = todayCount,
                    pendingReservationsCount = pendingCount,
                    availableTurnsCount = turns.size,
                    operationalTablesCount = tables.size,
                    availableGamesCount = games.size,
                    alertMessage = alertMessage,
                    isLoading = false,
                    isPartialData = isPartialData,
                    lastSyncLabel = "Actualizado ahora"
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lógica derivada privada
    // -------------------------------------------------------------------------

    /**
     * Devuelve la primera reserva futura (fecha >= hoy) con estado no terminal.
     * Si hay varias con la misma fecha, prioriza la de menor turnoNombre (orden natural).
     */
    private fun computeNextReservation(
        reservations: List<ReservationListItemUi>,
        today: String
    ): ReservationListItemUi? {
        val terminalStates = setOf("CANCELADA", "COMPLETADA")
        return reservations
            .filter { it.fecha >= today && it.estado.uppercase() !in terminalStates }
            .minByOrNull { it.fecha + "_" + it.turnoNombre }
    }

    /**
     * Construye un mensaje de alerta accionable basado en el estado global.
     * Para ADMIN prioriza información operativa (pendientes, actividad de hoy).
     * Para USER muestra información personal de sus próximas reservas.
     * Retorna null si no hay nada relevante que comunicar.
     */
    private fun buildAlertMessage(
        today: String,
        nextReservation: ReservationListItemUi?,
        availableTurnsCount: Int,
        todayCount: Int,
        pendingCount: Int
    ): String? {
        return if (role == UserRole.ADMIN) {
            when {
                pendingCount > 0 ->
                    "🔴 Hay $pendingCount reserva${if (pendingCount != 1) "s" else ""} pendiente${if (pendingCount != 1) "s" else ""} de revisión"
                todayCount > 0 ->
                    "📅 Hoy hay $todayCount reserva${if (todayCount != 1) "s" else ""} activa${if (todayCount != 1) "s" else ""} en el local"
                else -> null
            }
        } else {
            when {
                nextReservation?.fecha == today ->
                    "Tu próxima reserva es hoy en turno ${nextReservation.turnoNombre} · Mesa ${nextReservation.mesaNumero ?: "—"}"
                nextReservation == null && availableTurnsCount == 0 ->
                    "No hay turnos disponibles para hoy"
                nextReservation == null && todayCount == 0 ->
                    "No tienes reservas próximas. ¡Reserva ahora tu mesa!"
                else -> null
            }
        }
    }

    companion object {
        private fun todayString(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        private fun buildDateLabel(): String {
            val raw = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"))
                .format(Date())
            return raw.replaceFirstChar { it.uppercase() }
        }
    }
}

/**
 * Fábrica para [DashboardViewModel].
 */
class DashboardViewModelFactory(
    private val reservationRepository: ReservationRepository,
    private val gameRepository: GameRepository,
    private val role: UserRole
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(reservationRepository, gameRepository, role) as T
}

