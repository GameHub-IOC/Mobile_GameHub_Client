package ioc.andresgq.gamehubmobile.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ioc.andresgq.gamehubmobile.ui.state.UiState

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    userTypeFromRoute: String,
    onLogoutSuccess: () -> Unit,
    onCloseApp: () -> Unit
) {
    val logoutState by viewModel.logoutState.collectAsState()
    var userName by remember { mutableStateOf("") }

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
        onLogoutClick = viewModel::logout,
        onCloseApp = onCloseApp
    )
}

@Composable
fun HomeScreen(
    userName: String,
    userType: String,
    logoutState: UiState<Unit>,
    onLogoutClick: () -> Unit,
    onCloseApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Bienvenido, $userName",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(text = "Tipo de usuario: $userType")

        AsyncImage(
            model = "https://api.dicebear.com/9.x/thumbs/svg?seed=$userType",
            contentDescription = "Miniatura de perfil",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Crop
        )

        Button(onClick = onLogoutClick, enabled = logoutState !is UiState.Loading) {
            Text("Cerrar sesión")
        }
        Button(onClick = onCloseApp) {
            Text("Cerrar aplicación")
        }

        if (logoutState is UiState.Error) {
            Text(
                text = logoutState.message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}