package ioc.andresgq.gamehubmobile.ui.screens.gamecatalog

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ioc.andresgq.gamehubmobile.BuildConfig
import ioc.andresgq.gamehubmobile.ui.state.UiState

/**
 * Pantalla del catálogo completo de juegos.
 *
 * Muestra un [LazyColumn] de tarjetas de juego con filtros por texto,
 * categoría y disponibilidad. Gestiona los cuatro estados posibles del
 * [catalogState]: carga, error, éxito vacío y éxito con resultados.
 *
 * @param catalogState estado actual del catálogo.
 * @param onGameClick  callback invocado al pulsar una tarjeta, con el id del juego.
 */
@Composable
fun GameListScreen(
    catalogState: UiState<List<GameItemUi>>,
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

    when (catalogState) {
        UiState.Idle, UiState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is UiState.Error -> Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(catalogState.message, color = MaterialTheme.colorScheme.error)
        }

        is UiState.Success -> LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

            if (filteredGames.isEmpty()) {
                item {
                    Text(
                        text = "No hay juegos para los filtros actuales.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredGames, key = { it.id }) { game ->
                    GameCard(game = game, onClick = { onGameClick(game.id) })
                }
            }
        }
    }
}

/**
 * Tarjeta de juego individual para el listado del catálogo.
 */
@Composable
internal fun GameCard(game: GameItemUi, onClick: () -> Unit) {
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
 * Resuelve la URL completa de la imagen de un juego a partir de su ruta relativa.
 *
 * Si ya es una URL absoluta se devuelve tal cual; si es una ruta relativa
 * (p.ej. `"catan.jpg"`), se construye el endpoint del backend correspondiente.
 *
 * @param rutaImagen ruta relativa o URL absoluta de la imagen.
 * @return URL completa lista para [AsyncImage], o `null` si no hay imagen.
 */
fun resolveGameImageUrl(rutaImagen: String?): String? {
    val raw = rutaImagen?.trim().orEmpty()
    if (raw.isBlank()) return null
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    val encoded = Uri.encode(raw)
    return "$base/juegos/imagen/$encoded"
}

