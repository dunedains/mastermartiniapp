package cl.duoc.mastermartini.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.local.AppSettings
import cl.duoc.mastermartini.data.local.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado global: ¿se terminó de leer DataStore?, ¿hay sesión?, opciones de accesibilidad. */
sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val settings: AppSettings) : MainUiState {
        val isLogged: Boolean get() = settings.loggedUserId != null
    }
}

class MainViewModel(settings: SettingsDataStore) : ViewModel() {
    val state: StateFlow<MainUiState> = settings.settings
        .map<AppSettings, MainUiState> { MainUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)
}
