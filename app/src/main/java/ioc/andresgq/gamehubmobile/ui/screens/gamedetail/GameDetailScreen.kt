package ioc.andresgq.gamehubmobile.ui.screens.gamedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ioc.andresgq.gamehubmobile.R
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameItemUi
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.resolveGameImageUrl
import ioc.andresgq.gamehubmobile.ui.components.ErrorStateBlock
import ioc.andresgq.gamehubmobile.ui.components.LoadingStateBlock
import ioc.andresgq.gamehubmobile.ui.state.UiState

/**
 * Punto de entrada composable para la pantalla de detalle de un juego.
 *
 * Observa el [UiState] del [GameDetailViewModel] y delega la
 * presentación a [GameDetailScreen].
 *
 * @param viewModel        ViewModel que gestiona la carga del juego.
 * @param onNavigateBack   callback para volver a la pantalla anterior.
 */
@Composable
fun GameDetailRoute(
    viewModel: GameDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()

    GameDetailScreen(
        gameState = gameState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::loadGame
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameState: UiState<GameItemUi>,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (gameState as? UiState.Success)?.data?.nombre ?: "Detalle del juego"
                    Text(text = title, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (gameState) {
                UiState.Idle, UiState.Loading -> LoadingStateBlock(label = "Cargando detalle...")

                is UiState.Error -> ErrorStateBlock(
                    message = gameState.message,
                    onRetry = onRetry
                )

                is UiState.Success -> GameDetailContent(game = gameState.data)
            }
        }
    }
}

/**
 * Composable que muestra el contenido del detalle de un juego.
 *
 * Muestra la imagen **original** (no miniatura), nombre, disponibilidad,
 * categoría, jugadores, descripción y observaciones del juego.
 *
 * @param game juego a mostrar.
 */
@Composable
private fun GameDetailContent(game: GameItemUi) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Imagen original del juego (ocupa todo el ancho)
        AsyncImage(
            model = resolveGameImageUrl(game.rutaImagen),
            contentDescription = "Imagen de ${game.nombre}",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder)
        )

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre del juego
            Text(
                text = game.nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Etiqueta de disponibilidad
            val (labelText, labelColor) = if (game.disponible) {
                "Disponible" to MaterialTheme.colorScheme.primary
            } else {
                "No disponible" to MaterialTheme.colorScheme.error
            }
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelLarge,
                color = labelColor
            )

            HorizontalDivider()

            // Categoría y jugadores
            DetailRow(label = "Categoría", value = game.categoria)
            DetailRow(label = "Jugadores", value = game.numJugadores)

            // Descripción (opcional)
            if (!game.descripcion.isNullOrBlank()) {
                HorizontalDivider()
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = game.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Observaciones (estado físico de la copia, relevante para ADMIN)
            if (!game.observaciones.isNullOrBlank()) {
                HorizontalDivider()
                Text(
                    text = "Observaciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = game.observaciones,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Composable que muestra una fila de detalle con un [label] y un [value].
 *
 * @param label etiqueta a mostrar.
 * @param value valor a mostrar.
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}