package ioc.andresgq.gamehubmobile.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.ui.screens.reservations.ReservationListItemUi

/**
 * Pantalla principal del dashboard para el usuario autenticado.
 *
 * Secciones (de arriba a abajo):
 * 1. Header contextual: saludo + fecha + rol + estado de sincronización.
 * 2. Banner de datos parciales o error crítico (condicional).
 * 3. Alerta accionable contextual (condicional).
 * 4. KPI strip: mesas operativas | turnos | juegos disponibles.
 * 5. Próxima reserva (o empty state con CTA directo).
 * 6. Últimas reservas con chips de estado por colores.
 * 7. Acciones rápidas (Reservar / Ver historial).
 *
 * @param uiState           estado agregado producido por [DashboardViewModel].
 * @param onReserveNow      navega al wizard de reserva.
 * @param onViewReservations navega al listado de reservas.
 * @param onGoToManagement  navega a la sección de gestión (solo disponible para ADMIN).
 * @param onRefresh         fuerza recarga de todos los datos.
 */
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onReserveNow: () -> Unit,
    onViewReservations: () -> Unit,
    onGoToManagement: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ─── 1. Header ───────────────────────────────────────────────────────
        item {
            DashboardHeader(
                username = uiState.username,
                currentDateLabel = uiState.currentDateLabel,
                role = uiState.role,
                lastSyncLabel = uiState.lastSyncLabel,
                isLoading = uiState.isLoading,
                onRefresh = onRefresh
            )
        }

        // ─── 2. Banner de estado ─────────────────────────────────────────────
        if (uiState.errorMessage != null) {
            item { StatusBanner(message = uiState.errorMessage, isError = true) }
        } else if (uiState.isPartialData) {
            item {
                StatusBanner(
                    message = "Algunos datos no están disponibles · conexión parcial",
                    isError = false
                )
            }
        }

        // ─── 3. Alerta accionable ────────────────────────────────────────────
        if (!uiState.alertMessage.isNullOrBlank()) {
            item { AlertBanner(message = uiState.alertMessage) }
        }

        // ─── 4. KPI strip ────────────────────────────────────────────────────
        item {
            KpiStrip(
                role = uiState.role,
                tablesCount = uiState.operationalTablesCount,
                turnsCount = uiState.availableTurnsCount,
                gamesCount = uiState.availableGamesCount,
                todayCount = uiState.todayReservationsCount,
                pendingCount = uiState.pendingReservationsCount,
                isLoading = uiState.isLoading
            )
        }

        // ─── 5. Próxima reserva ──────────────────────────────────────────────
        item {
            NextReservationSection(
                role = uiState.role,
                reservation = uiState.nextReservation,
                isLoading = uiState.isLoading,
                onReserveNow = onReserveNow,
                onViewReservations = onViewReservations
            )
        }

        // ─── 6. Últimas reservas ─────────────────────────────────────────────
        if (uiState.recentReservations.isNotEmpty() || uiState.isLoading) {
            item {
                RecentReservationsSection(
                    role = uiState.role,
                    reservations = uiState.recentReservations,
                    isLoading = uiState.isLoading,
                    onViewAll = onViewReservations
                )
            }
        }

        // ─── 7. Acciones rápidas ─────────────────────────────────────────────
        item {
            QuickActionsSection(
                role = uiState.role,
                onReserveNow = onReserveNow,
                onViewReservations = onViewReservations,
                onGoToManagement = onGoToManagement
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sección 1: Header contextual
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(
    username: String,
    currentDateLabel: String,
    role: UserRole,
    lastSyncLabel: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (username.isBlank()) "Hola 👋" else "Hola, $username 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (currentDateLabel.isNotBlank()) {
                    Text(
                        text = currentDateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.CenterVertically),
                    strokeWidth = 2.dp
                )
            } else {
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("↻ Actualizar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoleBadge(role = role)
            if (lastSyncLabel.isNotBlank()) {
                Text(
                    text = lastSyncLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val label = if (role == UserRole.ADMIN) "ADMIN" else "USUARIO"
    val containerColor = if (role == UserRole.ADMIN)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (role == UserRole.ADMIN)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sección 2: Banners de estado / datos parciales
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusBanner(message: String, isError: Boolean) {
    val containerColor = if (isError)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isError)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (isError) "⚠️" else "ℹ️", style = MaterialTheme.typography.bodyMedium)
            Text(text = message, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sección 3: Alerta contextual accionable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AlertBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔔", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sección 4: KPI strip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KpiStrip(
    role: UserRole,
    tablesCount: Int,
    turnsCount: Int,
    gamesCount: Int,
    todayCount: Int,
    pendingCount: Int,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (role == UserRole.ADMIN) "Operación hoy" else "Disponibilidad",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (role == UserRole.ADMIN) {
                KpiCard(
                    emoji = "📋",
                    label = "Reservas hoy",
                    value = if (isLoading) "…" else todayCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    emoji = "⏳",
                    label = "Pendientes",
                    value = if (isLoading) "…" else pendingCount.toString(),
                    highlight = pendingCount > 0,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    emoji = "🪑",
                    label = "Mesas op.",
                    value = if (isLoading) "…" else tablesCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            } else {
                KpiCard(
                    emoji = "🪑",
                    label = "Mesas",
                    value = if (isLoading) "…" else tablesCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    emoji = "⏰",
                    label = "Turnos",
                    value = if (isLoading) "…" else turnsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    emoji = "🎲",
                    label = "Juegos",
                    value = if (isLoading) "…" else gamesCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    ElevatedCard(
        modifier = modifier,
        colors = if (highlight)
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        else CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlight) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sección 5: Próxima reserva
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NextReservationSection(
    role: UserRole,
    reservation: ReservationListItemUi?,
    isLoading: Boolean,
    onReserveNow: () -> Unit,
    onViewReservations: () -> Unit
) {
    val isAdmin = role == UserRole.ADMIN
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (isAdmin) "Próxima reserva activa" else "Tu próxima reserva",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        when {
            isLoading -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            reservation == null -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = if (isAdmin) "Sin reservas activas próximas"
                                   else "Sin reservas próximas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isAdmin)
                                       "No hay reservas pendientes o confirmadas en los próximos días"
                                   else "Elige fecha, turno y mesa para reservar tu próxima partida",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isAdmin) {
                            Button(
                                onClick = onReserveNow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🎲 Reservar ahora")
                            }
                        }
                    }
                }
            }

            else -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Fila fecha + estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅 ${reservation.fecha}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            ReservationStatusChip(estado = reservation.estado)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Para ADMIN: mostrar el usuario propietario de la reserva
                        if (isAdmin && reservation.usuarioNombre.isNotBlank()) {
                            ReservationDetailRow(
                                icon = "👤",
                                label = "Usuario",
                                value = reservation.usuarioNombre
                            )
                        }

                        ReservationDetailRow(
                            icon = "⏰",
                            label = "Turno",
                            value = reservation.turnoNombre
                        )
                        ReservationDetailRow(
                            icon = "🪑",
                            label = "Mesa",
                            value = reservation.mesaNumero?.toString() ?: "—"
                        )
                        val juegoText = reservation.juegoNombre
                            .takeIf { it.isNotBlank() && it != "Sin juego" }
                            ?: "Sin preferencia"
                        ReservationDetailRow(
                            icon = "🎲",
                            label = "Juego",
                            value = juegoText
                        )

                        OutlinedButton(
                            onClick = onViewReservations,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(if (isAdmin) "Ver todas las reservas" else "Ver mis reservas")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationDetailRow(icon: String, label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chip de estado de reserva (reutilizable)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Chip visual con color semántico según el estado de la reserva.
 *
 * Paleta:
 * - CONFIRMADA → verde
 * - PENDIENTE  → naranja
 * - CANCELADA  → rojo
 * - COMPLETADA → azul
 * - otros      → gris neutro
 */
@Composable
fun ReservationStatusChip(estado: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = statusColorPair(estado)
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        contentColor = textColor,
        modifier = modifier
    ) {
        Text(
            text = estado.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun statusColorPair(estado: String): Pair<Color, Color> =
    when (estado.uppercase()) {
        "CONFIRMADA" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "PENDIENTE"  -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "CANCELADA"  -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "COMPLETADA" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        else         -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

// ─────────────────────────────────────────────────────────────────────────────
// Sección 6: Últimas reservas (historial corto)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecentReservationsSection(
    role: UserRole,
    reservations: List<ReservationListItemUi>,
    isLoading: Boolean,
    onViewAll: () -> Unit
) {
    val isAdmin = role == UserRole.ADMIN
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAdmin) "Actividad reciente del local" else "Últimas reservas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onViewAll) {
                Text(
                    if (isAdmin) "Ver historial global" else "Ver historial completo",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                reservations.isEmpty() -> {
                    Text(
                        text = if (isAdmin)
                                   "Sin reservas recientes en el sistema"
                               else "Aún no tienes reservas registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    Column {
                        reservations.forEachIndexed { index, item ->
                            ReservationTimelineItem(item = item, showUser = isAdmin)
                            if (index < reservations.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationTimelineItem(item: ReservationListItemUi, showUser: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "📅 ${item.fecha}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${item.turnoNombre} · Mesa ${item.mesaNumero ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showUser && item.usuarioNombre.isNotBlank()) {
                Text(
                    text = "👤 ${item.usuarioNombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ReservationStatusChip(estado = item.estado)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sección 7: Acciones rápidas
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(
    role: UserRole,
    onReserveNow: () -> Unit,
    onViewReservations: () -> Unit,
    onGoToManagement: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Acciones rápidas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (role == UserRole.ADMIN) {
            // Fila 1: Ver reservas + Gestión
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReservations,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📋 Reservas")
                }
                Button(
                    onClick = onGoToManagement,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⚙️ Gestión")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReserveNow,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🎲 Reservar")
                }
                OutlinedButton(
                    onClick = onViewReservations,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📋 Mis reservas")
                }
            }
        }
    }
}
