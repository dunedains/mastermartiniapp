package cl.duoc.mastermartini.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.ServiceType
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.data.repository.ReservationRepository
import cl.duoc.mastermartini.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User? = null,
    val featured: LearnItem? = null,
    val recipes: List<LearnItem> = emptyList(),
    val nextEvent: ServiceItem? = null,
    val nextEventReserved: Boolean = false,
    val largeText: Boolean = false,
)

/** Inicio personalizado según el tipo de negocio del usuario. */
class HomeViewModel(
    users: UserRepository,
    private val content: ContentRepository,
    reservations: ReservationRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        users.currentUser, reservations.reservations, settings.settings,
    ) { user, reserved, prefs ->
        val all = content.learnItems()
        val preferred = user?.let { content.learnCategoriesFor(it.businessType) }.orEmpty()
        val recipes = all.filter { it.kind == LearnKind.RECIPE }
            .sortedByDescending { it.category in preferred }
        val event = content.services().firstOrNull { it.type == ServiceType.EVENT }
        HomeUiState(
            user = user,
            featured = all.firstOrNull { it.kind == LearnKind.VIDEO && it.isNew } ?: all.first(),
            recipes = recipes,
            nextEvent = event,
            nextEventReserved = event != null && reserved.any { it.serviceId == event.id },
            largeText = prefs.largeText,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleLargeText() {
        viewModelScope.launch { settings.setLargeText(!settings.settings.first().largeText) }
    }
}
