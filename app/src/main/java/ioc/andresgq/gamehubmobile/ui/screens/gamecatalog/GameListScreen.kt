package ioc.andresgq.gamehubmobile.ui.screens.gamecatalog

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ioc.andresgq.gamehubmobile.BuildConfig
import ioc.andresgq.gamehubmobile.R
import ioc.andresgq.gamehubmobile.ui.state.UiState

/** Criterios de ordenación disponibles para el catálogo. */
private enum class SortOrder(val label: String) {
    NONE("Por defecto"),
    NOMBRE_ASC("A → Z"),
    NOMBRE_DESC("Z → A"),
    DISPONIBLES_FIRST("Disponibles primero")
}

/**
 * Pantalla del catálogo completo de juegos.
 *
 * Muestra un [LazyColumn] de tarjetas de juego con:
 * - Filtros por texto, categoría y disponibilidad.
 * - Ordenación por nombre (asc/desc) o disponibilidad.
 * - Contador de resultados visibles y botón para limpiar filtros activos.
 * - Pull-to-refresh para recargar el catálogo sin abandonar la pantalla.
 * - Miniaturas (`thumb_`) en las tarjetas e imagen original en el detalle.
 * - Botón "Reservar" directo en cada tarjeta de juego disponible.
 *
 * @param catalogState    estado actual del catálogo.
 * @param onGameClick     callback invocado al pulsar el cuerpo de la tarjeta, con el id del juego.
 * @param onRefresh       callback para forzar una recarga del catálogo.
 * @param onReserveClick  callback invocado al pulsar "Reservar" en una tarjeta; `null` oculta el botón.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(
    catalogState: UiState<List<GameItemUi>>,
    onGameClick: (Long) -> Unit,
    onRefresh: () -> Unit = {},
    onReserveClick: ((GameItemUi) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Todas") }
    var onlyAvailable by rememberSaveable { mutableStateOf(false) }
    // Guardamos el nombre de la opción para que sea serializable con rememberSaveable
    var sortOrderName by rememberSaveable { mutableStateOf(SortOrder.NONE.name) }
    val sortOrder = SortOrder.valueOf(sortOrderName)

    // Mantiene los últimos datos válidos visibles durante el pull-to-refresh
    var lastGoodGames by remember { mutableStateOf<List<GameItemUi>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(catalogState) {
        when (catalogState) {
            is UiState.Success -> {
                lastGoodGames = catalogState.data
                isRefreshing = false
            }
            is UiState.Error -> isRefreshing = false
            else -> Unit
        }
    }

    val categories = listOf("Todas") + lastGoodGames.map { it.categoria }.distinct().sorted()
    val hasActiveFilters = query.isNotBlank()
            || selectedCategory != "Todas"
            || onlyAvailable
            || sortOrder != SortOrder.NONE

    val filteredGames = lastGoodGames
        .filter { game ->
            val matchesQuery = query.isBlank()
                    || game.nombre.contains(query, ignoreCase = true)
                    || (game.descripcion?.contains(query, ignoreCase = true) == true)
            val matchesCategory = selectedCategory == "Todas" || game.categoria == selectedCategory
            val matchesAvailability = !onlyAvailable || game.disponible
            matchesQuery && matchesCategory && matchesAvailability
        }
        .let { list ->
            when (sortOrder) {
                SortOrder.NOMBRE_ASC -> list.sortedBy { it.nombre.lowercase() }
                SortOrder.NOMBRE_DESC -> list.sortedByDescending { it.nombre.lowercase() }
                SortOrder.DISPONIBLES_FIRST -> list.sortedByDescending { it.disponible }
                SortOrder.NONE -> list
            }
        }

    when {
        // Carga inicial sin datos en caché → spinner centrado
        catalogState is UiState.Loading && lastGoodGames.isEmpty() -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        // Error sin datos en caché → mensaje de error centrado
        catalogState is UiState.Error && lastGoodGames.isEmpty() -> Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(catalogState.message, color = MaterialTheme.colorScheme.error)
        }

        // Datos disponibles (incluye estado de refresh) → lista con pull-to-refresh
        else -> PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                onRefresh()
            },
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Barra de búsqueda
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar por nombre o descripción") },
                        singleLine = true
                    )
                }

                // Chips de categoría
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

                // Chips de disponibilidad + ordenación
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = onlyAvailable,
                                onClick = { onlyAvailable = !onlyAvailable },
                                label = { Text("Solo disponibles") }
                            )
                        }
                        items(SortOrder.entries.filter { it != SortOrder.NONE }) { order ->
                            FilterChip(
                                selected = sortOrderName == order.name,
                                onClick = {
                                    sortOrderName = if (sortOrderName == order.name) {
                                        SortOrder.NONE.name
                                    } else {
                                        order.name
                                    }
                                },
                                label = { Text(order.label) }
                            )
                        }
                    }
                }

                // Contador de resultados + botón limpiar filtros
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val total = lastGoodGames.size
                        val showing = filteredGames.size
                        Text(
                            text = if (showing == total) {
                                "$total juego${if (total != 1) "s" else ""}"
                            } else {
                                "Mostrando $showing de $total"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasActiveFilters) {
                            TextButton(onClick = {
                                query = ""
                                selectedCategory = "Todas"
                                onlyAvailable = false
                                sortOrderName = SortOrder.NONE.name
                            }) {
                                Text("Limpiar filtros")
                            }
                        }
                    }
                }

                // Lista de juegos o estado vacío
                if (filteredGames.isEmpty()) {
                    item {
                        Text(
                            text = "No hay juegos para los filtros actuales.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(filteredGames, key = { it.id }) { game ->
                        GameCard(
                            game = game,
                            onClick = { onGameClick(game.id) },
                            onReserveClick = onReserveClick?.let { cb ->
                                if (game.disponible) ({ cb(game) }) else null
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

/**
 * Tarjeta de juego individual para el listado del catálogo.
 *
 * Usa la miniatura (`thumb_`) del juego para cargar la imagen de manera eficiente.
 * Si la imagen no está disponible o falla, muestra un placeholder genérico.
 * Cuando [onReserveClick] no es `null`, muestra un botón "Reservar" en la parte
 * inferior de la tarjeta (solo para juegos disponibles).
 *
 * @param game            datos del juego a mostrar.
 * @param onClick         acción al pulsar el cuerpo de la tarjeta (abre el detalle).
 * @param onReserveClick  acción al pulsar "Reservar"; `null` oculta el botón.
 */
@Composable
internal fun GameCard(
    game: GameItemUi,
    onClick: () -> Unit,
    onReserveClick: (() -> Unit)? = null
) {
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
                model = resolveGameThumbnailUrl(game.rutaImagen),
                contentDescription = "Imagen de ${game.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_image_placeholder),
                error = painterResource(R.drawable.ic_image_placeholder)
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

            // Botón de reserva rápida: solo visible para juegos disponibles
            if (onReserveClick != null) {
                Button(
                    onClick = onReserveClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reservar")
                }
            }
        }
    }
}

/**
 * Resuelve la URL de la **miniatura** (`thumb_`) de un juego a partir de su ruta relativa.
 *
 * En listas se usa la miniatura para ahorrar ancho de banda; en el detalle
 * se usa [resolveGameImageUrl] con la imagen original.
 *
 * **Nota:** mientras el servidor no genere archivos `thumb_*` automáticamente al
 * subir imágenes, este método delega en [resolveGameImageUrl] para evitar errores
 * 404. Cuando el backend esté listo, basta con sustituir el cuerpo por la
 * implementación comentada a continuación.
 *
 * @param rutaImagen ruta relativa o URL absoluta de la imagen.
 * @return URL completa de la miniatura (o de la imagen original como fallback), o `null` si no hay imagen.
 */
fun resolveGameThumbnailUrl(rutaImagen: String?): String? {
    // TODO: descomentar cuando el servidor genere miniaturas thumb_* automáticamente:
    // val raw = rutaImagen?.trim().orEmpty()
    // if (raw.isBlank()) return null
    // if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
    // val base = BuildConfig.API_BASE_URL.trimEnd('/')
    // val encoded = Uri.encode("thumb_$raw")
    // return "$base/juegos/imagen/$encoded"

    // Fallback: usa la imagen original hasta que el servidor soporte miniaturas
    return resolveGameImageUrl(rutaImagen)
}

/**
 * Resuelve la URL completa de la imagen **original** de un juego a partir de su ruta relativa.
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
