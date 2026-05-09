package ioc.andresgq.gamehubmobile.ui.screens.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ioc.andresgq.gamehubmobile.ui.components.EmptyStateBlock
import ioc.andresgq.gamehubmobile.ui.components.ErrorStateBlock
import ioc.andresgq.gamehubmobile.ui.components.LoadingStateBlock
import ioc.andresgq.gamehubmobile.ui.screens.dashboard.ReservationStatusChip
import ioc.andresgq.gamehubmobile.ui.state.UiState

/** Estados posibles que el admin puede filtrar. */
private val ADMIN_STATUS_OPTIONS = listOf("PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA")

/** ────────────────────────────────────────────────────────────────
 *  Pantalla de reservas del usuario estándar.
 *  Muestra sus propias reservas y permite cancelarlas individualmente.
 *  ──────────────────────────────────────────────────────────────── */
@Composable
fun MyReservationsScreen(
    state: UiState<List<ReservationListItemUi>>,
    deleteState: UiState<Unit>,
    onReload: () -> Unit,
    onDelete: (Long) -> Unit,
    onConsumeDeleteState: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-consume estado de éxito del borrado para no bloquear la UI
    LaunchedEffect(deleteState) {
        if (deleteState is UiState.Success) onConsumeDeleteState()
    }

    when (state) {
        UiState.Idle, UiState.Loading -> LoadingStateBlock(
            modifier = modifier,
            label = "Cargando reservas..."
        )

        is UiState.Error -> ErrorStateBlock(
            message = state.message,
            onRetry = onReload,
            modifier = modifier
        )

        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyStateBlock(
                    title = "Mis reservas",
                    description = "No tienes ninguna reserva todavía.",
                    onReload = onReload,
                    modifier = modifier
                )
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Text("Mis reservas", style = MaterialTheme.typography.titleLarge)
                    }

                    if (deleteState is UiState.Error) {
                        item {
                            Text(
                                text = deleteState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    items(state.data, key = { it.id }) { reservation ->
                        UserReservationCard(
                            reservation = reservation,
                            deleteState = deleteState,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

/** ────────────────────────────────────────────────────────────────
 *  Pantalla de reservas globales para el administrador.
 *
 *  Incluye:
 *  - Barra de búsqueda por nombre de usuario (filtro cliente).
 *  - Chips de filtro por estado (PENDIENTE / CONFIRMADA / CANCELADA / COMPLETADA).
 *  - Contador de resultados filtrados.
 *  - Tarjeta por reserva con botón "Cancelar" (DELETE /reservas/{id}).
 *  ──────────────────────────────────────────────────────────────── */
@Composable
fun AdminReservationsScreen(
    state: UiState<List<ReservationListItemUi>>,
    deleteState: UiState<Unit>,
    statusFilter: String?,
    userFilter: String,
    onReload: () -> Unit,
    onDelete: (Long) -> Unit,
    onConsumeDeleteState: () -> Unit,
    onStatusFilterChange: (String?) -> Unit,
    onUserFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-consume estado de éxito del borrado
    LaunchedEffect(deleteState) {
        if (deleteState is UiState.Success) onConsumeDeleteState()
    }

    when (state) {
        UiState.Idle, UiState.Loading -> LoadingStateBlock(
            modifier = modifier,
            label = "Cargando reservas..."
        )

        is UiState.Error -> ErrorStateBlock(
            message = state.message,
            onRetry = onReload,
            modifier = modifier
        )

        is UiState.Success -> {
            val reservations = state.data

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // ── Cabecera ───────────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reservas globales",
                            style = MaterialTheme.typography.titleLarge
                        )
                        TextButton(onClick = onReload) {
                            Text("Recargar")
                        }
                    }
                }

                // ── Barra de búsqueda por usuario ──────────────────────────────
                item {
                    OutlinedTextField(
                        value = userFilter,
                        onValueChange = onUserFilterChange,
                        label = { Text("Buscar por usuario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ── Chips de filtro por estado ─────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Filtrar por estado:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = statusFilter == null,
                                    onClick = { onStatusFilterChange(null) },
                                    label = { Text("Todos") }
                                )
                            }
                            items(ADMIN_STATUS_OPTIONS) { status ->
                                FilterChip(
                                    selected = statusFilter == status,
                                    onClick = {
                                        onStatusFilterChange(
                                            if (statusFilter == status) null else status
                                        )
                                    },
                                    label = { Text(status) }
                                )
                            }
                        }
                    }
                }

                // ── Contador de resultados ─────────────────────────────────────
                item {
                    val total = reservations.size
                    val filtroActivo = statusFilter != null || userFilter.isNotBlank()
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = if (filtroActivo)
                            "$total resultado${if (total != 1) "s" else ""} filtrados"
                        else
                            "$total reserva${if (total != 1) "s" else ""} en total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // ── Error del último borrado ───────────────────────────────────
                if (deleteState is UiState.Error) {
                    item {
                        Text(
                            text = deleteState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // ── Lista de reservas ─────────────────────────────────────────
                if (reservations.isEmpty()) {
                    item {
                        Spacer(Modifier.height(32.dp))
                        Text(
                            text = "No hay reservas que coincidan con los filtros aplicados.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(reservations, key = { it.id }) { reservation ->
                        AdminReservationCard(
                            reservation = reservation,
                            deleteState = deleteState,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta de reserva para usuario estándar (con botón Cancelar)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UserReservationCard(
    reservation: ReservationListItemUi,
    deleteState: UiState<Unit>,
    onDelete: (Long) -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        CancelConfirmDialog(
            reservationId = reservation.id,
            onConfirm = {
                showConfirm = false
                onDelete(reservation.id)
            },
            onDismiss = { showConfirm = false }
        )
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reserva #${reservation.id}",
                    fontWeight = FontWeight.SemiBold
                )
                ReservationStatusChip(estado = reservation.estado)
            }
            Text("Fecha: ${reservation.fecha}")
            Text("Turno: ${reservation.turnoNombre}")
            Text("Mesa: ${reservation.mesaNumero ?: "–"}")
            Text("Juego: ${reservation.juegoNombre}")

            // Solo permitir cancelar si no está ya cancelada o completada
            val puedeCancel = reservation.estado.uppercase() !in setOf("CANCELADA", "COMPLETADA")
            if (puedeCancel) {
                Spacer(Modifier.height(4.dp))
                val isDeleting = deleteState is UiState.Loading
                OutlinedButton(
                    onClick = { showConfirm = true },
                    enabled = !isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cancelar reserva")
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta de reserva para administrador (con badge usuario + botón Cancelar)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminReservationCard(
    reservation: ReservationListItemUi,
    deleteState: UiState<Unit>,
    onDelete: (Long) -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        CancelConfirmDialog(
            reservationId = reservation.id,
            onConfirm = {
                showConfirm = false
                onDelete(reservation.id)
            },
            onDismiss = { showConfirm = false }
        )
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Cabecera: id + estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reserva #${reservation.id}",
                    fontWeight = FontWeight.SemiBold
                )
                ReservationStatusChip(estado = reservation.estado)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Datos de la reserva
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "📅 ${reservation.fecha}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "⏱ ${reservation.turnoNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🏓 Mesa ${reservation.mesaNumero ?: "–"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "🎲 ${reservation.juegoNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "👤 ${reservation.usuarioNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Botón cancelar (admin puede cancelar en cualquier estado no final)
            val puedeCancel = reservation.estado.uppercase() !in setOf("CANCELADA", "COMPLETADA")
            if (puedeCancel) {
                Spacer(Modifier.height(4.dp))
                val isDeleting = deleteState is UiState.Loading
                Button(
                    onClick = { showConfirm = true },
                    enabled = !isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        Text("Cancelar reserva")
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Diálogo de confirmación antes de cancelar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CancelConfirmDialog(
    reservationId: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancelar reserva") },
        text = {
            Text("¿Seguro que quieres cancelar la reserva #$reservationId? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sí, cancelar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No, volver")
            }
        }
    )
}
