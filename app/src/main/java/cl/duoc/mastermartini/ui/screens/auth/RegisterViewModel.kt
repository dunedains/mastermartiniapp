package cl.duoc.mastermartini.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.repository.AuthResult
import cl.duoc.mastermartini.data.repository.NewUser
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: FieldState = FieldState(),
    val email: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    val confirm: FieldState = FieldState(),
    val businessType: BusinessType? = null,
    val businessTypeError: String? = null,
    val businessSize: BusinessSize? = null,
    val businessSizeError: String? = null,
    val interests: Set<String> = emptySet(),
    val interestsError: String? = null,
    val accepted: Boolean = false,
    val acceptedError: String? = null,
    val loading: Boolean = false,
    /** Aumenta en cada intento fallido de envío → dispara la animación de sacudida. */
    val failedAttempts: Int = 0,
) {
    /** Progreso del formulario (0..1) para la barra superior. */
    val progress: Float
        get() {
            val checks = listOf(
                name.value.isNotBlank() && Validators.name(name.value).isValid,
                Validators.email(email.value).isValid,
                Validators.phone(phone.value).isValid,
                Validators.password(password.value).isValid,
                confirm.value.isNotEmpty() && confirm.value == password.value,
                businessType != null,
                businessSize != null,
                interests.isNotEmpty(),
                accepted,
            )
            return checks.count { it } / checks.size.toFloat()
        }
}

sealed interface RegisterEvent {
    data object Registered : RegisterEvent
}

/**
 * ViewModel del registro. Toda la lógica de validación vive aquí (usando [Validators]);
 * la pantalla solo dibuja el [RegisterUiState] y reenvía eventos del usuario.
 */
class RegisterViewModel(private val users: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onNameChange(v: String) = _state.update { it.copy(name = it.name.update(v, Validators::name)) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = it.email.update(v, Validators::email)) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = it.phone.update(v, Validators::phone)) }
    fun onPasswordChange(v: String) = _state.update {
        it.copy(
            password = it.password.update(v, Validators::password),
            // si ya escribió la confirmación, la revalidamos contra la nueva contraseña
            confirm = it.confirm.update(it.confirm.value) { c -> Validators.passwordConfirmation(v, c) },
        )
    }
    fun onConfirmChange(v: String) = _state.update {
        it.copy(confirm = it.confirm.update(v) { c -> Validators.passwordConfirmation(it.password.value, c) })
    }

    fun onNameBlur() = _state.update { it.copy(name = it.name.touch(Validators::name)) }
    fun onEmailBlur() = _state.update { it.copy(email = it.email.touch(Validators::email)) }
    fun onPhoneBlur() = _state.update { it.copy(phone = it.phone.touch(Validators::phone)) }
    fun onPasswordBlur() = _state.update { it.copy(password = it.password.touch(Validators::password)) }
    fun onConfirmBlur() = _state.update {
        it.copy(confirm = it.confirm.touch { c -> Validators.passwordConfirmation(it.password.value, c) })
    }

    fun onBusinessType(type: BusinessType) = _state.update { it.copy(businessType = type, businessTypeError = null) }
    fun onBusinessSize(size: BusinessSize) = _state.update { it.copy(businessSize = size, businessSizeError = null) }
    fun onToggleInterest(interest: String) = _state.update {
        val set = if (interest in it.interests) it.interests - interest else it.interests + interest
        it.copy(interests = set, interestsError = if (it.interestsError != null) Validators.atLeast(set, 1, MSG_INTEREST).error else null)
    }
    fun onAcceptedChange(v: Boolean) = _state.update {
        it.copy(accepted = v, acceptedError = if (it.acceptedError != null) Validators.acceptedTerms(v).error else null)
    }

    fun submit() {
        val s = _state.value
        val validated = s.copy(
            name = s.name.touch(Validators::name),
            email = s.email.touch(Validators::email),
            phone = s.phone.touch(Validators::phone),
            password = s.password.touch(Validators::password),
            confirm = s.confirm.touch { Validators.passwordConfirmation(s.password.value, it) },
            businessTypeError = Validators.selected(s.businessType, "Elige el tipo de negocio").error,
            businessSizeError = Validators.selected(s.businessSize, "Elige el tamaño").error,
            interestsError = Validators.atLeast(s.interests, 1, MSG_INTEREST).error,
            acceptedError = Validators.acceptedTerms(s.accepted).error,
        )
        val hasErrors = listOf(validated.name, validated.email, validated.phone, validated.password, validated.confirm)
            .any { it.error != null } ||
            listOf(validated.businessTypeError, validated.businessSizeError, validated.interestsError, validated.acceptedError)
                .any { it != null }

        if (hasErrors) {
            _state.value = validated.copy(failedAttempts = s.failedAttempts + 1)
            return
        }

        _state.value = validated.copy(loading = true)
        viewModelScope.launch {
            val result = users.register(
                NewUser(
                    name = s.name.value, email = s.email.value, phone = s.phone.value,
                    password = s.password.value, businessType = s.businessType!!,
                    businessSize = s.businessSize!!, interests = s.interests.toList(),
                    acceptedTerms = s.accepted,
                )
            )
            when (result) {
                is AuthResult.Success -> _events.send(RegisterEvent.Registered)
                AuthResult.EmailTaken -> _state.update {
                    it.copy(email = it.email.copy(error = "Este correo ya tiene una cuenta"), failedAttempts = it.failedAttempts + 1)
                }
                AuthResult.InvalidCredentials -> Unit
            }
            _state.update { it.copy(loading = false) }
        }
    }

    private companion object {
        const val MSG_INTEREST = "Elige al menos un tema"
    }
}
