package cl.duoc.mastermartini.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.model.QuoteLine
import cl.duoc.mastermartini.data.repository.QuoteRepository
import cl.duoc.mastermartini.data.repository.QuoteRequest
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.device.NotificationHelper
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

val COMUNAS = listOf(
    "Santiago", "Providencia", "Las Condes", "Ñuñoa", "Maipú", "La Florida", "Puente Alto",
    "San Bernardo", "Quilicura", "Valparaíso", "Viña del Mar", "Concepción", "Temuco",
)

data class QuoteFormState(
    val contactName: FieldState = FieldState(),
    val address: FieldState = FieldState(),
    val comuna: FieldState = FieldState(),
    val dateMillis: Long? = null,
    val dateError: String? = null,
    val notes: FieldState = FieldState(),
    val loading: Boolean = false,
    val failedAttempts: Int = 0,
    val sentCode: String? = null,
    val sentSummary: String = "",
    val notified: Boolean = false,
) {
    val dateLabel: String get() = dateMillis?.let { formatDate(it) }.orEmpty()
}

fun formatDate(millisUtc: Long): String =
    Instant.ofEpochMilli(millisUtc).atZone(ZoneOffset.UTC).toLocalDate()
        .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "CL")))
        .replaceFirstChar { it.uppercase() }

class QuoteFormViewModel(
    private val users: UserRepository,
    private val quotes: QuoteRepository,
    private val notifications: NotificationHelper,
) : ViewModel() {

    val lines: StateFlow<List<QuoteLine>> = quotes.lines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(QuoteFormState())
    val state: StateFlow<QuoteFormState> = _state.asStateFlow()

    init {
        // Prellenamos el nombre de contacto con el del usuario registrado.
        viewModelScope.launch {
            val user = users.currentUser.filterNotNull().first()
            _state.update { if (it.contactName.value.isEmpty()) it.copy(contactName = it.contactName.update(user.name, Validators::name)) else it }
        }
    }

    fun onContact(v: String) = _state.update { it.copy(contactName = it.contactName.update(v, Validators::name)) }
    fun onAddress(v: String) = _state.update { it.copy(address = it.address.update(v, Validators::address)) }
    fun onComuna(v: String) = _state.update { it.copy(comuna = it.comuna.update(v, ::validateComuna).copy(touched = true)) }
    fun onNotes(v: String) = _state.update { it.copy(notes = it.notes.update(v) { n -> Validators.optionalText(n, 300) }.copy(touched = true)) }
    fun onDate(millis: Long?) = _state.update { it.copy(dateMillis = millis, dateError = Validators.futureWorkingDate(millis).error) }

    fun onContactBlur() = _state.update { it.copy(contactName = it.contactName.touch(Validators::name)) }
    fun onAddressBlur() = _state.update { it.copy(address = it.address.touch(Validators::address)) }

    fun removeLine(productId: String) {
        viewModelScope.launch { quotes.removeProduct(productId) }
    }

    fun submit() {
        val s = _state.value
        val v = s.copy(
            contactName = s.contactName.touch(Validators::name),
            address = s.address.touch(Validators::address),
            comuna = s.comuna.touch(::validateComuna),
            notes = s.notes.touch { Validators.optionalText(it, 300) },
            dateError = Validators.futureWorkingDate(s.dateMillis).error,
        )
        val invalid = listOf(v.contactName, v.address, v.comuna, v.notes).any { it.error != null } ||
            v.dateError != null || lines.value.isEmpty()
        if (invalid) {
            _state.value = v.copy(failedAttempts = s.failedAttempts + 1)
            return
        }
        _state.value = v.copy(loading = true)
        viewModelScope.launch {
            val summary = lines.value.joinToString("\n") { "• ${it.productName} ${it.format} x${it.quantity}" }
            val code = quotes.send(
                QuoteRequest(v.contactName.value, v.address.value, v.comuna.value, v.dateLabel, v.notes.value)
            )
            val notified = code != null && notifications.show(
                "Cotización $code enviada",
                "Ventas te contactará antes del ${v.dateLabel.lowercase()} para confirmar precios y despacho.",
            )
            _state.update { it.copy(loading = false, sentCode = code, sentSummary = summary, notified = notified) }
        }
    }

    private fun validateComuna(v: String) = Validators.oneOf(v, COMUNAS, "Elige una comuna")
}
