package ioc.andresgq.gamehubmobile.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ioc.andresgq.gamehubmobile.BuildConfig
import ioc.andresgq.gamehubmobile.ui.state.UiState

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    userTypeFromRoute: String,
    onLogoutSuccess: () -> Unit,
    onGameClick: (Long) -> Unit,
    onCloseApp: () -> Unit
) {
    val logoutState by viewModel.logoutState.collectAsState()
    val catalogState by viewModel.catalogState.collectAsState()
    var userName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userName = viewModel.getCurrentUser()?.username.orEmpty()
    }

    LaunchedEffect(logoutState) {
        if (logoutState is UiState.Success) {
            onLogoutSuccess()
        }
    }

    HomeScreen(
        userName = userName,
        userType = userTypeFromRoute,
        logoutState = logoutState,
        catalogState = catalogState,
        onLogoutClick = viewModel::logout,
        onGameClick = onGameClick,
        onReloadCatalog = { viewModel.loadCatalog(force = true) },
        onCloseApp = onCloseApp
    )
}

@Composable
fun HomeScreen(
    userName: String,
    userType: String,
    logoutState: UiState<Unit>,
    catalogState: UiState<List<GameItemUi>>,
    onLogoutClick: () -> Unit,
    onReloadCatalog: () -> Unit,
    onCloseApp: () -> Unit,
    onGameClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Todas") }
    var onlyAvailable by rememberSaveable { mutableStateOf(false) }

    val allGames = (catalogState as? UiState.Success)?.data.orEmpty()
    val categories = listOf("Todas") + allGames.map { it.categoria }.distinct().sorted()

    val filteredGames = allGames.filter { game ->
        val matchesQuery = query.isBlank() ||
                game.nombre.contains(query, ignoreCase = true) ||
                (game.descripcion?.contains(query, ignoreCase = true) == true)

        val matchesCategory = selectedCategory == "Todas" || game.categoria == selectedCategory
        val matchesAvailability = !onlyAvailable || game.disponible

        matchesQuery && matchesCategory && matchesAvailability
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Bienvenido, $userName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tipo de usuario: $userType",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onReloadCatalog) { Text("Recargar") }
                Button(onClick = onLogoutClick, enabled = logoutState !is UiState.Loading) {
                    Text("Cerrar sesion")
                }
                OutlinedButton(onClick = onCloseApp) { Text("Salir") }
            }
        }

        if (logoutState is UiState.Error) {
            item {
                Text(
                    text = logoutState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Text(
                text = "Catalogo de juegos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar por nombre o descripcion") },
                singleLine = true
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }
        }

        item {
            FilterChip(
                selected = onlyAvailable,
                onClick = { onlyAvailable = !onlyAvailable },
                label = { Text("Solo disponibles") }
            )
        }

        when (catalogState) {
            UiState.Idle, UiState.Loading -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is UiState.Error -> {
                item {
                    Text(
                        text = catalogState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is UiState.Success -> {
                if (filteredGames.isEmpty()) {
                    item {
                        Text(
                            text = "No hay juegos para los filtros actuales.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(filteredGames, key = { it.id }) { game ->
                        GameCard(game = game, onClick = { onGameClick(game.id) })  // <-- ACTUALIZAR
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: GameItemUi, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = resolveGameImageUrl(game.rutaImagen),
                contentDescription = "Imagen de ${game.nombre}",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = game.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${game.categoria}  |  Jugadores: ${game.numJugadores}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (game.disponible) "Disponible" else "No disponible",
                style = MaterialTheme.typography.labelLarge,
                color = if (game.disponible) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            if (!game.descripcion.isNullOrBlank()) {
                Text(
                    text = game.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Resuelve la URL de imagen de un juego.
 *
 * @param rutaImagen ruta relativa de la imagen en el backend.
 * @return URL completa de la imagen, o `null` si no hay imagen.
 *
 */
fun resolveGameImageUrl(rutaImagen: String?): String? {
    val raw = rutaImagen?.trim().orEmpty()
    if (raw.isBlank()) return null

    // Si ya es URL completa, la usamos tal cual.
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw

    // Si viene del seeder como "catan.jpg", construimos endpoint de backend.
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    val encoded = Uri.encode(raw)
    return "$base/juegos/imagen/$encoded"
}