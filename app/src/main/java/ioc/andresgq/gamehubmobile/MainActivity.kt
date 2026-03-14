package ioc.andresgq.gamehubmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ioc.andresgq.gamehubmobile.ui.navigation.AppNavHost
import ioc.andresgq.gamehubmobile.ui.theme.GameHubMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as GameHubApplication).container

        enableEdgeToEdge()
        setContent {
            GameHubMobileTheme {
                AppNavHost(
                    authRepository = appContainer.authRepository,
                    onCloseApp = { finish() }
                )
            }
        }
    }
}
