package cl.duoc.mastermartini.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.BrandHeader
import cl.duoc.mastermartini.ui.components.MMTextField
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.ScreenTitle
import cl.duoc.mastermartini.ui.components.shake

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    vm: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.loggedIn.collect { onLoggedIn() } }

    // Entrada animada del formulario al abrir la pantalla.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BrandHeader()
        Spacer(Modifier.height(24.dp))
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically { it / 4 }) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                ScreenTitle("Hola de nuevo", subtitle = "Ingresa para ver tus contenidos, cotizaciones y reservas.")
                MMTextField(
                    "Correo", s.email, vm::onEmailChange, Icons.Outlined.Email,
                    placeholder = "correo@ejemplo.cl", keyboardType = KeyboardType.Email, onFocusLost = vm::onEmailBlur,
                )
                MMTextField(
                    "Contraseña", s.password, vm::onPasswordChange, Icons.Outlined.Lock,
                    isPassword = true, keyboardType = KeyboardType.Password, imeAction = ImeAction.Done,
                )
                AnimatedVisibility(s.formError != null) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(s.formError.orEmpty(), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                PrimaryButton(
                    "Ingresar", vm::submit, loading = s.loading,
                    icon = Icons.AutoMirrored.Outlined.Login, modifier = Modifier.shake(s.failedAttempts),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onGoToRegister) {
                        Text("Créala aquí", fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                    }
                }
            }
        }
    }
}
