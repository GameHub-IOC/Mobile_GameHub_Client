package ioc.andresgq.gamehubmobile.ui.screens.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ioc.andresgq.gamehubmobile.ui.components.EmptyStateBlock
import ioc.andresgq.gamehubmobile.ui.components.ErrorStateBlock
import ioc.andresgq.gamehubmobile.ui.components.LoadingStateBlock
import ioc.andresgq.gamehubmobile.ui.state.UiState

@Composable
fun MyReservationsScreen(
    state: UiState<List<ReservationListItemUi>>,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReservationListScreenContent(
        title = "Mis reservas",
        state = state,
        onReload = onReload,
        showUserColumn = false,
        modifier = modifier
    )
}

@Composable
fun AdminReservationsScreen(
    state: UiState<List<ReservationListItemUi>>,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReservationListScreenContent(
        title = "Reservas globales",
        state = state,
        onReload = onReload,
        showUserColumn = true,
        modifier = modifier
    )
}

@Composable
private fun ReservationListScreenContent(
    title: String,
    state: UiState<List<ReservationListItemUi>>,
    onReload: () -> Unit,
    showUserColumn: Boolean,
    modifier: Modifier = Modifier
) {
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
                    title = title,
                    description = "No hay reservas para mostrar.",
                    onReload = onReload,
                    modifier = modifier
                )
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                    }
                    items(state.data, key = { it.id }) { reservation ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Reserva #${reservation.id}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("Fecha: ${reservation.fecha}")
                                Text("Estado: ${reservation.estado}")
                                Text("Turno: ${reservation.turnoNombre}")
                                Text("Mesa: ${reservation.mesaNumero ?: "-"}")
                                Text("Juego: ${reservation.juegoNombre}")
                                if (showUserColumn) {
                                    Text("Usuario: ${reservation.usuarioNombre}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

