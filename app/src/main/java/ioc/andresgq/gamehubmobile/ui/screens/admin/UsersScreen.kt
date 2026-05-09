package ioc.andresgq.gamehubmobile.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ioc.andresgq.gamehubmobile.ui.state.UiState

// ─────────────────────────────────────────────────────────────────────────────
//  Pantalla principal
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla de gestión de usuarios accesible solo para administradores.
 *
 * Permite:
 * - Visualizar el listado de usuarios con nombre y rol actual.
 * - Cambiar el rol de cualquier usuario (USER ↔ ADMIN) con un solo toque.
 * - Eliminar usuarios, con diálogo de confirmación previo.
 * - Filtrar el listado en tiempo real por nombre.
 *
 * @param viewModel instancia de [UsersViewModel] que proporciona el estado y las acciones.
 * @param modifier  modificador opcional para el composable raíz.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel,
    modifier: Modifier = Modifier
) {
    val usersState by viewModel.usersState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Filtramos por nombre en tiempo real
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Usuario cuya eliminación está pendiente de confirmar
    var deletingUser by remember { mutableStateOf<UserItemUi?>(null) }

    // Controla la visibilidad del diálogo de creación
    var showCreateDialog by remember { mutableStateOf(false) }

    // Muestra el resultado de la última operación como snackbar
    LaunchedEffect(operationState) {
        when (operationState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Operación completada correctamente")
                viewModel.consumeOperationState()
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
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear usuario")
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

            // ── Cabecera ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gestión de usuarios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Consulta y cambia los roles de los usuarios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { viewModel.loadUsers(force = true) }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Recargar usuarios",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Barra de búsqueda ───────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── Contenido principal ─────────────────────────────────────────
            when (val state = usersState) {

                UiState.Idle, UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = { viewModel.loadUsers(force = true) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val filtered = state.data.filter { user ->
                        searchQuery.isBlank() ||
                            user.nombre.contains(searchQuery.trim(), ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isBlank())
                                    "No hay usuarios registrados"
                                else
                                    "Ningún usuario coincide con \"${searchQuery.trim()}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Contador de resultados
                        Text(
                            text = "${filtered.size} usuario${if (filtered.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filtered, key = { it.id }) { user ->
                                UserManagementItem(
                                    user = user,
                                    isSaving = operationState is UiState.Loading,
                                    onToggleRole = { viewModel.toggleRole(user) },
                                    onDelete = { deletingUser = user }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    // ── Diálogo de confirmación de eliminación ──────────────────────────────
    deletingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { deletingUser = null },
            title = { Text("Eliminar usuario") },
            text = {
                Text(
                    "¿Seguro que quieres eliminar al usuario \"${user.nombre}\"? " +
                        "Esta acción es permanente y no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user.id)
                        deletingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deletingUser = null }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo de creación de usuario ──────────────────────────────────────
    if (showCreateDialog) {
        CreateUserDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { nombre, password, rol ->
                viewModel.createUser(nombre, password, rol)
                showCreateDialog = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Diálogo de creación de usuario
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Diálogo modal para crear un nuevo usuario desde el panel de administración.
 *
 * Contiene campos para nombre de usuario, contraseña y un selector de rol.
 * El botón de confirmar queda deshabilitado mientras algún campo esté vacío.
 *
 * @param onDismiss callback al cerrar sin guardar.
 * @param onCreate  callback con (nombre, password, rol) al confirmar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (nombre: String, password: String, rol: String) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf("USER") }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    val roles = listOf("USER", "ADMIN")
    val isValid = nombre.isNotBlank() && password.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear nuevo usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                // Selector de rol con ExposedDropdownMenu
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        roles.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    rol = option
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(nombre.trim(), password, rol) },
                enabled = isValid
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Item de listado — usuario
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tarjeta que representa a un usuario en el listado de gestión.
 *
 * Muestra el avatar con iniciales, nombre, chip de rol y las acciones disponibles:
 * - Botón de cambio de rol (USER ↔ ADMIN).
 * - Icono de eliminación.
 *
 * @param user        datos del usuario a mostrar.
 * @param isSaving    `true` mientras hay una operación en curso (deshabilita controles).
 * @param onToggleRole callback invocado al pulsar el botón de cambio de rol.
 * @param onDelete     callback invocado al pulsar el icono de eliminar.
 */
@Composable
private fun UserManagementItem(
    user: UserItemUi,
    isSaving: Boolean,
    onToggleRole: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = user.rol == "ADMIN"

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ── Avatar con iniciales ────────────────────────────────────────
            val initials = user.nombre
                .trim()
                .split("\\s+".toRegex())
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
                .ifBlank { "?" }

            val avatarBg = if (isAdmin)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer

            val avatarContent = if (isAdmin)
                MaterialTheme.colorScheme.onTertiaryContainer
            else
                MaterialTheme.colorScheme.onSecondaryContainer

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .then(
                        Modifier.padding(0.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = avatarBg,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = avatarContent
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // ── Nombre + chip de rol ────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                RolChip(rol = user.rol)
            }

            // ── Botón cambio de rol ─────────────────────────────────────────
            val toggleLabel = if (isAdmin) "→ USER" else "→ ADMIN"
            val toggleColors = if (isAdmin) {
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            } else {
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            }

            TextButton(
                onClick = onToggleRole,
                enabled = !isSaving,
                colors = toggleColors
            ) {
                Text(
                    text = toggleLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Botón eliminar ──────────────────────────────────────────────
            IconButton(onClick = onDelete, enabled = !isSaving) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar ${user.nombre}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Chip de rol
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Pequeño chip con fondo de color que indica el rol del usuario.
 *
 * - ADMIN → contenedor terciario (naranja/verde según el tema).
 * - USER  → contenedor secundario.
 */
@Composable
private fun RolChip(rol: String, modifier: Modifier = Modifier) {
    val isAdmin = rol == "ADMIN"
    val bg = if (isAdmin)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer
    val content = if (isAdmin)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        contentColor = content,
        modifier = modifier
    ) {
        Text(
            text = rol,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
