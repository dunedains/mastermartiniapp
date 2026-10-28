package cl.duoc.mastermartini.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.repository.AuthResult
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

data class LoginUiState(
    val email: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    val loading: Boolean = false,
    val formError: String? = null,
    val failedAttempts: Int = 0,
)

class LoginViewModel(private val users: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _loggedIn = Channel<Unit>(Channel.BUFFERED)
    val loggedIn = _loggedIn.receiveAsFlow()

    fun onEmailChange(v: String) = _state.update { it.copy(email = it.email.update(v, Validators::email), formError = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = it.password.update(v, Validators::loginPassword), formError = null) }
    fun onEmailBlur() = _state.update { it.copy(email = it.email.touch(Validators::email)) }

    fun submit() {
        val s = _state.value
        val v = s.copy(email = s.email.touch(Validators::email), password = s.password.touch(Validators::loginPassword))
        if (v.email.error != null || v.password.error != null) {
            _state.value = v.copy(failedAttempts = s.failedAttempts + 1)
            return
        }
        _state.value = v.copy(loading = true)
        viewModelScope.launch {
            when (users.login(s.email.value, s.password.value)) {
                is AuthResult.Success -> _loggedIn.send(Unit)
                else -> _state.update {
                    it.copy(formError = "Correo o contraseña incorrectos", failedAttempts = it.failedAttempts + 1)
                }
            }
            _state.update { it.copy(loading = false) }
        }
    }
}
