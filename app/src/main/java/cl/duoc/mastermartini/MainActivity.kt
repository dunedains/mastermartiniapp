package cl.duoc.mastermartini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.MainUiState
import cl.duoc.mastermartini.ui.MainViewModel
import cl.duoc.mastermartini.ui.navigation.AppNavigation
import cl.duoc.mastermartini.ui.theme.MasterMartiniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by vm.state.collectAsStateWithLifecycle()
            val settings = (state as? MainUiState.Ready)?.settings

            MasterMartiniTheme(
                largeText = settings?.largeText ?: false,
                highContrast = settings?.highContrast ?: false,
            ) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    // Mientras se lee la sesión guardada mostramos un loader; luego entra la app con fundido.
                    Crossfade(targetState = state is MainUiState.Ready, label = "appStart") { ready ->
                        if (!ready) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else {
                            // La pantalla inicial se decide UNA vez (sesión guardada → Inicio; si no → Login).
                            AppNavigation(isLogged = (state as MainUiState.Ready).isLogged)
                        }
                    }
                }
            }
        }
    }
}
