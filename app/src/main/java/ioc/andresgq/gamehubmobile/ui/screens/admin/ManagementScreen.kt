package ioc.andresgq.gamehubmobile.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ioc.andresgq.gamehubmobile.data.remote.dto.CategoriaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.GameRequestDto
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameItemUi
import ioc.andresgq.gamehubmobile.ui.state.UiState

// ─────────────────────────────────────────────────────────────────────────────
//  Pantalla principal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ManagementScreen(
    viewModel: GameManagementViewModel,
    modifier: Modifier = Modifier
) {
    val gamesState by viewModel.gamesState.collectAsState()
    val categoriasState by viewModel.categoriasState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var editingGame by remember { mutableStateOf<GameItemUi?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var deletingGameId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(operationState) {
        when (operationState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Operación completada correctamente")
                viewModel.consumeOperationState()
                showFormDialog = false
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((operationState as UiState.Error).message)
                viewModel.consumeOperationState()
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingGame = null
                showFormDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir juego")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Gestión de juegos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Añade, edita o elimina juegos del catálogo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            when (val state = gamesState) {
                UiState.Idle, UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadGames(force = true) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val games = state.data
                    if (games.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No hay juegos en el catálogo. Pulsa + para añadir uno.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(games, key = { it.id }) { game ->
                                GameManagementItem(
                                    game = game,
                                    onEdit = {
                                        editingGame = game
                                        showFormDialog = true
                                    },
                                    onDelete = { deletingGameId = game.id }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        val categorias = (categoriasState as? UiState.Success)?.data ?: emptyList()
        val isSaving = operationState is UiState.Loading

        GameFormDialog(
            game = editingGame,
            categorias = categorias,
            isSaving = isSaving,
            onDismiss = { showFormDialog = false },
            onSubmit = { request ->
                val current = editingGame
                if (current == null) viewModel.createGame(request)
                else viewModel.updateGame(current.id, request)
            }
        )
    }

    deletingGameId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingGameId = null },
            title = { Text("Eliminar juego") },
            text = { Text("¿Seguro que quieres eliminar este juego? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGame(id)
                        deletingGameId = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deletingGameId = null }) { Text("Cancelar") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Item de listado
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GameManagementItem(
    game: GameItemUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${game.categoria} · ${game.numJugadores} jug.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val disponibleColor = if (game.disponible)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
                Text(
                    text = if (game.disponible) "Disponible" else "No disponible",
                    style = MaterialTheme.typography.labelSmall,
                    color = disponibleColor
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Editar ${game.nombre}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar ${game.nombre}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Diálogo de creación / edición
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameFormDialog(
    game: GameItemUi?,
    categorias: List<CategoriaDto>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (GameRequestDto) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf(game?.nombre ?: "") }
    var numJugadores by rememberSaveable { mutableStateOf(game?.numJugadores ?: "") }
    var disponible by rememberSaveable { mutableStateOf(game?.disponible ?: true) }
    var descripcion by rememberSaveable { mutableStateOf(game?.descripcion ?: "") }
    var observaciones by rememberSaveable { mutableStateOf(game?.observaciones ?: "") }

    val defaultCategoria = categorias.firstOrNull { it.nombre == game?.categoria }
        ?: categorias.firstOrNull()
    var selectedCategoria by remember { mutableStateOf(defaultCategoria) }
    var categoriaExpanded by remember { mutableStateOf(false) }

    val nombreError = nombre.isBlank()
    val jugadoresError = numJugadores.isBlank()
    val categoriaError = selectedCategoria == null

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (game == null) "Nuevo juego" else "Editar juego") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    isError = nombreError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nombreError) {
                    Text(
                        "El nombre es obligatorio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = numJugadores,
                    onValueChange = { numJugadores = it },
                    label = { Text("Nº jugadores *") },
                    isError = jugadoresError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                if (jugadoresError) {
                    Text(
                        "Indica el número de jugadores",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = categoriaExpanded,
                    onExpandedChange = { categoriaExpanded = !categoriaExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategoria?.nombre ?: "Selecciona categoría",
                        onValueChange = {},
                        readOnly = true,
                        isError = categoriaError,
                        label = { Text("Categoría *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    selectedCategoria = cat
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }
                if (categoriaError) {
                    Text(
                        "Selecciona una categoría",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = disponible, onCheckedChange = { disponible = it })
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cat = selectedCategoria ?: return@Button
                    onSubmit(
                        GameRequestDto(
                            nombre = nombre.trim(),
                            numJugadores = numJugadores.trim(),
                            categoria = cat,
                            disponible = disponible,
                            descripcion = descripcion.trim().takeIf { it.isNotBlank() },
                            observaciones = observaciones.trim().takeIf { it.isNotBlank() }
                        )
                    )
                },
                enabled = !isSaving && !nombreError && !jugadoresError && !categoriaError
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (game == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}
