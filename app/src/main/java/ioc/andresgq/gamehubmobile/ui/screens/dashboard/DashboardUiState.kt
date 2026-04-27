package ioc.andresgq.gamehubmobile.ui.screens.dashboard

import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.ui.screens.reservations.ReservationListItemUi

/**
 * Estado agregado del Dashboard.
 *
 * Combina los datos de reservas del usuario, disponibilidad de turnos, mesas y juegos
 * en un único objeto inmutable que la UI consume sin lógica adicional.
 *
 * @param username          nombre del usuario activo (actualizado desde ProfileViewModel).
 * @param role              rol del usuario autenticado.
 * @param currentDateLabel  fecha actual formateada en español para el header.
 * @param lastSyncLabel     texto descriptivo del último refresco ("Actualizado ahora", etc.).
 * @param nextReservation   primera reserva futura no cancelada/completada, o null.
 * @param recentReservations últimas 5 reservas ordenadas por fecha descendente.
 * @param todayReservationsCount número de reservas del usuario para hoy.
 * @param operationalTablesCount número de mesas operativas en el sistema.
 * @param availableTurnsCount número de turnos configurados.
 * @param availableGamesCount número de juegos disponibles en catálogo.
 * @param alertMessage      mensaje contextual accionable (puede ser null si no hay alerta).
 * @param isLoading         true mientras alguna fuente de datos sigue cargando.
 * @param isPartialData     true si al menos una llamada falló (datos incompletos).
 * @param errorMessage      error global crítico, distinto de los parciales.
 */
data class DashboardUiState(
    val username: String = "",
    val role: UserRole = UserRole.USER,
    val currentDateLabel: String = "",
    val lastSyncLabel: String = "",

    // Próxima reserva pendiente/confirmada
    val nextReservation: ReservationListItemUi? = null,

    // Historial reciente (hasta 5 elementos, más reciente primero)
    val recentReservations: List<ReservationListItemUi> = emptyList(),

    // KPIs instantáneos de disponibilidad
    val todayReservationsCount: Int = 0,
    val operationalTablesCount: Int = 0,
    val availableTurnsCount: Int = 0,
    val availableGamesCount: Int = 0,

    // Alerta contextual accionable
    val alertMessage: String? = null,

    // Estado de carga / errores
    val isLoading: Boolean = true,
    val isPartialData: Boolean = false,
    val errorMessage: String? = null
)

