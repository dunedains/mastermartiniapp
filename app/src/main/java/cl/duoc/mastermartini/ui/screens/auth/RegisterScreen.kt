package cl.duoc.mastermartini.ui.screens.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.LEARNING_INTERESTS
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.BrandHeader
import cl.duoc.mastermartini.ui.components.ChipGroup
import cl.duoc.mastermartini.ui.components.FieldMessage
import cl.duoc.mastermartini.ui.components.MMTextField
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.ScreenTitle
import cl.duoc.mastermartini.ui.components.SegmentedChoice
import cl.duoc.mastermartini.ui.components.shake

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onGoToLogin: () -> Unit,
    vm: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.events.collect { onRegistered() } }
    val progress by animateFloatAsState(s.progress, label = "formProgress")

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BrandHeader()
        ScreenTitle("Crea tu cuenta", subtitle = "Recibe recetas, videos y cursos pensados para tu negocio.")
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        MMTextField("Nombre", s.name, vm::onNameChange, Icons.Outlined.Person, placeholder = "Ej: Rosa Pérez", onFocusLost = vm::onNameBlur)
        MMTextField(
            "Correo", s.email, vm::onEmailChange, Icons.Outlined.Email, placeholder = "correo@ejemplo.cl",
            keyboardType = KeyboardType.Email, onFocusLost = vm::onEmailBlur,
        )
        MMTextField(
            "Teléfono", s.phone, vm::onPhoneChange, Icons.Outlined.Phone, placeholder = "+56 9 1234 5678",
            keyboardType = KeyboardType.Phone, onFocusLost = vm::onPhoneBlur,
        )
        MMTextField(
            "Contraseña", s.password, vm::onPasswordChange, Icons.Outlined.Lock, isPassword = true,
            helper = "Mínimo 8 caracteres, con letras y números", keyboardType = KeyboardType.Password,
            onFocusLost = vm::onPasswordBlur,
        )
        MMTextField(
            "Confirmar contraseña", s.confirm, vm::onConfirmChange, Icons.Outlined.LockReset, isPassword = true,
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done, onFocusLost = vm::onConfirmBlur,
        )

        ChipGroup(
            label = "¿Qué tipo de negocio tienes?",
            options = BusinessType.entries,
            isSelected = { it == s.businessType },
            onToggle = vm::onBusinessType,
            optionLabel = { it.label },
            error = s.businessTypeError,
        )
        SegmentedChoice(
            label = "Tamaño del negocio",
            options = BusinessSize.entries,
            selected = s.businessSize,
            onSelect = vm::onBusinessSize,
            optionLabel = { it.label },
            error = s.businessSizeError,
        )
        ChipGroup(
            label = "¿Qué te interesa aprender?",
            options = LEARNING_INTERESTS,
            isSelected = { it in s.interests },
            onToggle = vm::onToggleInterest,
            optionLabel = { it },
            error = s.interestsError,
            multi = true,
        )

        Column {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (s.acceptedError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                ),
                onClick = { vm.onAcceptedChange(!s.accepted) },
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Checkbox(checked = s.accepted, onCheckedChange = vm::onAcceptedChange)
                    Text(
                        "Acepto que Master Martini use mis datos para enviarme contenidos e invitaciones. " +
                            "Puedo darme de baja cuando quiera.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
            FieldMessage(error = s.acceptedError)
        }

        PrimaryButton(
            text = "Crear cuenta",
            onClick = vm::submit,
            loading = s.loading,
            icon = Icons.Outlined.PersonAdd,
            modifier = Modifier.shake(s.failedAttempts),
        )
        if (s.failedAttempts > 0 && s.progress < 1f) {
            Text(
                "Revisa los campos marcados en rojo",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onGoToLogin) {
                Text("Ingresar", fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
