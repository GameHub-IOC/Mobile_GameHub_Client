package ioc.andresgq.gamehubmobile.ui.screens.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.ui.state.UiState

@Composable
fun RegisterRoute(
    viewModel: RegisterViewModel,
    onLoginSuccess: (UserSession) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onLoginSuccess((uiState as UiState.Success<UserSession>).data)
        }
    }

    RegisterScreen(
        username = viewModel.username,
        password = viewModel.password,
        email = viewModel.email,
        confirmPassword = viewModel.confirmPassword,
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onEmailChange = viewModel::onEmailChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onLoginClick = viewModel::register,
        onDismissError = viewModel::clearError
    )
}

@Composable
fun RegisterScreen(
    username: String,
    password: String,
    email: String,
    confirmPassword: String,
    uiState: UiState<UserSession>,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
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
            text = "GameHub - Registro",
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

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

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

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text("Confirmar contraseña") },
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
                Text("Registrarse")
            }
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