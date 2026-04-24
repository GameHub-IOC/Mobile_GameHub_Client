package ioc.andresgq.gamehubmobile.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.ui.state.UiState

/**
 * Composable que representa la pantalla de inicio de sesión.
 *
 * @param viewModel El ViewModel asociado a la pantalla de inicio de sesión.
 * @param onLoginSuccess Callback que se llama cuando la autenticación es exitosa.
 * @param onNavigateToRegister Callback que se llama cuando se debe navegar a la pantalla de registro.
 */
@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onLoginSuccess: (UserSession) -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onLoginSuccess((uiState as UiState.Success<UserSession>).data)
        }
    }

    LoginScreen(
        username = viewModel.username,
        password = viewModel.password,
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onNavigateToRegister = onNavigateToRegister,
        onLoginClick = viewModel::login,
        onDismissError = viewModel::clearError,
    )
}

/**
 * Composable que representa la pantalla de inicio de sesión.
 *
 * @param username El nombre de usuario ingresado por el usuario.
 * @param password La contraseña ingresada por el usuario.
 * @param uiState El estado actual de la pantalla de inicio de sesión.
 * @param onUsernameChange Callback que se llama cuando cambia el nombre de usuario.
 * @param onPasswordChange Callback que se llama cuando cambia la contraseña.
 * @param onNavigateToRegister Callback que se llama cuando se debe navegar a la pantalla de registro.
 * @param onLoginClick Callback que se llama cuando se intenta iniciar sesión.
 * @param onDismissError Callback que se llama cuando se debe ocultar el mensaje de error.
 * @param modifier Modificador para personalizar la apariencia y el comportamiento de la pantalla.
 */
@Composable
fun LoginScreen(
    username: String,
    password: String,
    uiState: UiState<UserSession>,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginClick: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GameHub - Acceso",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            label = { Text("Usuario") },
            singleLine = true
        )

        @Suppress("DEPRECATION")
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        val isLoading = uiState is UiState.Loading

        Button(
            onClick = onLoginClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(2.dp))
            } else {
                Text("Entrar")
            }
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }

        if (uiState is UiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
            TextButton(onClick = onDismissError) {
                Text("Ocultar")
            }
        }
    }
}