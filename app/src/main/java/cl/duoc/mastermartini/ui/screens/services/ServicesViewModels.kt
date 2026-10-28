package cl.duoc.mastermartini.ui.screens.services

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.ServiceType
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.data.repository.ReservationRepository
import cl.duoc.mastermartini.data.repository.ReserveResult
import cl.duoc.mastermartini.device.NotificationHelper
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import cl.duoc.mastermartini.ui.screens.catalog.formatDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ===================== Listado de servicios =====================

data class ServicesUiState(
    val filter: ServiceType? = null,
    val services: List<ServiceItem> = emptyList(),
    val reservedIds: Set<String> = emptySet(),
    val confirming: ServiceItem? = null,
)

class ServicesViewModel(
    private val content: ContentRepository,
    private val reservations: ReservationRepository,
    private val notifications: NotificationHelper,
) : ViewModel() {

    private val filter = MutableStateFlow<ServiceType?>(null)
    private val confirming = MutableStateFlow<ServiceItem?>(null)

    val state: StateFlow<ServicesUiState> =
        combine(filter, confirming, reservations.reservations) { f, c, res ->
            ServicesUiState(
                filter = f,
                services = content.services().filter { f == null || it.type == f },
                reservedIds = res.map { it.serviceId }.toSet(),
                confirming = c,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ServicesUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun onFilter(type: ServiceType?) { filter.value = type }
    fun askEnroll(item: ServiceItem) { confirming.value = item }
    fun dismissEnroll() { confirming.value = null }

    fun confirmEnroll() {
        val item = confirming.value ?: return
        confirming.value = null
        viewModelScope.launch {
            val result = reservations.reserve(
                serviceId = item.id, title = item.title,
                dateLabel = item.dateLabel.orEmpty(), modality = "Presencial",
            )
            val msg = when (result) {
                ReserveResult.Success -> {
                    val shown = notifications.show(
                        "Inscripción confirmada",
                        "Te esperamos en ${item.title} · ${item.dateLabel}. ${item.detail}.",
                    )
                    if (shown) "¡Listo! Te inscribimos y te dejamos un recordatorio." else "¡Listo! Quedaste inscrita/o."
                }
                ReserveResult.AlreadyReserved -> "Ya estabas inscrita/o en este evento"
                ReserveResult.NotLogged -> "Inicia sesión para inscribirte"
            }
            _messages.send(msg)
        }
    }
}

// ===================== Formulario de reserva de asesoría =====================

enum class Modality(val label: String) { VIDEO("Videollamada"), ON_SITE("En mi local") }

val TIME_SLOTS = listOf("09:30", "11:00", "12:30", "15:00", "16:30")
val ADVISORY_TOPICS = listOf("Formulación", "Costos y rendimiento", "Uso de equipos", "Nuevos productos", "Conservación")

data class BookingUiState(
    val service: ServiceItem? = null,
    val dateMillis: Long? = null,
    val dateError: String? = null,
    val time: String? = null,
    val timeError: String? = null,
    val modality: Modality? = null,
    val modalityError: String? = null,
    val address: FieldState = FieldState(),
    val topic: String? = null,
    val topicError: String? = null,
    val description: FieldState = FieldState(),
    val loading: Boolean = false,
    val failedAttempts: Int = 0,
    val confirmed: Boolean = false,
    val notified: Boolean = false,
) {
    val dateLabel: String get() = dateMillis?.let(::formatDate).orEmpty()
}

class BookingViewModel(
    savedState: SavedStateHandle,
    content: ContentRepository,
    private val reservations: ReservationRepository,
    private val notifications: NotificationHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(
        BookingUiState(service = savedState.get<String>("id")?.let(content::service))
    )
    val state: StateFlow<BookingUiState> = _state.asStateFlow()

    private val descRule: (String) -> cl.duoc.mastermartini.domain.validation.ValidationResult = { d ->
        val req = Validators.required(d, "Cuéntanos qué necesitas", min = 10)
        if (!req.isValid) req else Validators.optionalText(d, 250)
    }

    fun onDate(m: Long?) = _state.update { it.copy(dateMillis = m, dateError = Validators.futureWorkingDate(m).error) }
    fun onTime(t: String) = _state.update { it.copy(time = t, timeError = null) }
    fun onModality(m: Modality) = _state.update { it.copy(modality = m, modalityError = null) }
    fun onAddress(v: String) = _state.update { it.copy(address = it.address.update(v, Validators::address)) }
    fun onAddressBlur() = _state.update { it.copy(address = it.address.touch(Validators::address)) }
    fun onTopic(t: String) = _state.update { it.copy(topic = t, topicError = null) }
    fun onDescription(v: String) = _state.update { it.copy(description = it.description.update(v, descRule)) }
    fun onDescriptionBlur() = _state.update { it.copy(description = it.description.touch(descRule)) }

    fun submit() {
        val s = _state.value
        val needsAddress = s.modality == Modality.ON_SITE
        val v = s.copy(
            dateError = Validators.futureWorkingDate(s.dateMillis).error,
            timeError = Validators.selected(s.time, "Elige un horario").error,
            modalityError = Validators.selected(s.modality, "Elige la modalidad").error,
            address = if (needsAddress) s.address.touch(Validators::address) else s.address.copy(error = null),
            topicError = Validators.selected(s.topic, "Elige un tema").error,
            description = s.description.touch(descRule),
        )
        val invalid = listOf(v.dateError, v.timeError, v.modalityError, v.topicError, v.description.error).any { it != null } ||
            (needsAddress && v.address.error != null)
        if (invalid) {
            _state.value = v.copy(failedAttempts = s.failedAttempts + 1)
            return
        }
        val service = s.service ?: return
        _state.value = v.copy(loading = true)
        viewModelScope.launch {
            val schedule = "${v.dateLabel} · ${v.time} h"
            reservations.reserve(
                serviceId = service.id,
                title = "${service.title} (${v.topic})",
                dateLabel = schedule,
                modality = v.modality!!.label + if (needsAddress) " · ${v.address.value}" else "",
                notes = v.description.value,
                allowRepeat = true,
            )
            val notified = notifications.show(
                "Asesoría reservada",
                "$schedule · ${v.modality.label}. Un chef te contactará para confirmar.",
            )
            _state.update { it.copy(loading = false, confirmed = true, notified = notified) }
        }
    }
}
