package cl.duoc.mastermartini.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.Order
import cl.duoc.mastermartini.data.model.Reservation
import cl.duoc.mastermartini.data.model.SentQuote
import cl.duoc.mastermartini.data.model.User
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.data.repository.QuoteRepository
import cl.duoc.mastermartini.data.repository.ReservationRepository
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ===================== Mi cuenta =====================

data class AccountUiState(
    val user: User? = null,
    val orders: List<Order> = emptyList(),
    val quotes: List<SentQuote> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val largeText: Boolean = false,
    val highContrast: Boolean = false,
    val salesPhone: String = "",
)

class AccountViewModel(
    private val users: UserRepository,
    private val content: ContentRepository,
    private val quotes: QuoteRepository,
    private val reservations: ReservationRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {

    val state: StateFlow<AccountUiState> = combine(
        users.currentUser, quotes.sentQuotes, reservations.reservations, settings.settings,
    ) { user, q, r, prefs ->
        AccountUiState(
            user = user,
            orders = content.sampleOrders(),
            quotes = q,
            reservations = r,
            largeText = prefs.largeText,
            highContrast = prefs.highContrast,
            salesPhone = content.salesPhone(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun setLargeText(v: Boolean) { viewModelScope.launch { settings.setLargeText(v) } }
    fun setHighContrast(v: Boolean) { viewModelScope.launch { settings.setHighContrast(v) } }

    /** "Repetir pedido": vuelve a cargar productos en la cotización. */
    fun repeatOrder(onDone: () -> Unit) {
        viewModelScope.launch {
            listOf("p-crema" to 2, "p-cobertura" to 1).forEach { (id, qty) ->
                content.product(id)?.let { quotes.setLine(it.id, it.name, it.formats.last(), qty) }
            }
            _messages.send("Agregamos los productos del pedido a tu cotización")
            onDone()
        }
    }

    fun cancelReservation(id: Long) {
        viewModelScope.launch {
            reservations.cancel(id)
            _messages.send("Reserva cancelada")
        }
    }

    fun logout() { viewModelScope.launch { users.logout() } }
}

// ===================== Editar perfil =====================

data class EditProfileUiState(
    val loaded: Boolean = false,
    val name: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val businessType: BusinessType? = null,
    val businessSize: BusinessSize? = null,
    val interests: Set<String> = emptySet(),
    val interestsError: String? = null,
    val saving: Boolean = false,
    val failedAttempts: Int = 0,
    val saved: Boolean = false,
)

class EditProfileViewModel(private val users: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val u = users.currentUser.filterNotNull().first()
            _state.value = EditProfileUiState(
                loaded = true,
                name = FieldState(u.name, touched = true),
                phone = FieldState(u.phone, touched = true),
                businessType = u.businessType,
                businessSize = u.businessSize,
                interests = u.interests.toSet(),
            )
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = it.name.update(v, Validators::name)) }
    fun onPhone(v: String) = _state.update { it.copy(phone = it.phone.update(v, Validators::phone)) }
    fun onType(t: BusinessType) = _state.update { it.copy(businessType = t) }
    fun onSize(s: BusinessSize) = _state.update { it.copy(businessSize = s) }
    fun onInterest(i: String) = _state.update {
        val set = if (i in it.interests) it.interests - i else it.interests + i
        it.copy(interests = set, interestsError = Validators.atLeast(set, 1, "Elige al menos un tema").error)
    }

    fun save() {
        val s = _state.value
        val v = s.copy(
            name = s.name.touch(Validators::name),
            phone = s.phone.touch(Validators::phone),
            interestsError = Validators.atLeast(s.interests, 1, "Elige al menos un tema").error,
        )
        if (v.name.error != null || v.phone.error != null || v.interestsError != null) {
            _state.value = v.copy(failedAttempts = s.failedAttempts + 1)
            return
        }
        _state.value = v.copy(saving = true)
        viewModelScope.launch {
            users.updateProfile(v.name.value, v.phone.value, v.businessType!!, v.businessSize!!, v.interests.toList())
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
