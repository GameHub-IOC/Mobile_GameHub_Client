package ioc.andresgq.gamehubmobile.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ManagementScreen(modifier: Modifier = Modifier) {
    val sections = listOf("Juegos", "Categorias", "Mesas", "Turnos")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Gestion", style = MaterialTheme.typography.titleLarge)
        Text(
            "Accesos administrativos para CRUD. La navegacion detallada se integra en la fase final.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        sections.forEach { name ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = name,
                    modifier = Modifier.padding(14.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

