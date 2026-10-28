package cl.duoc.mastermartini.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.ui.theme.LocalExtraColors

/**
 * Campo de texto estándar de la app con validación visual por campo:
 * - ícono a la izquierda que describe el dato,
 * - ✓ verde a la derecha cuando es válido, ⚠ rojo cuando hay error,
 * - mensaje de error animado bajo el campo.
 */
@Composable
fun MMTextField(
    label: String,
    field: FieldState,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helper: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    onFocusLost: () -> Unit = {},
    trailingAction: (@Composable () -> Unit)? = null,
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val extra = LocalExtraColors.current

    Column(modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = field.value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusLostOnce(onFocusLost)
                .semantics { if (field.showError) error(field.error ?: "") },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = { Icon(leadingIcon, contentDescription = null) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPassword) {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                            )
                        }
                    }
                    trailingAction?.invoke()
                    ValidationIcon(field)
                }
            },
            isError = field.showError,
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                // Campos blancos sobre fondo gris, sin borde hasta que se enfocan (estilo iOS).
                unfocusedBorderColor = if (field.showSuccess) extra.success else Color.Transparent,
                focusedBorderColor = if (field.showSuccess) extra.success else MaterialTheme.colorScheme.primary,
            ),
        )
        FieldMessage(error = if (field.showError) field.error else null, helper = helper)
    }
}

@Composable
private fun ValidationIcon(field: FieldState) {
    val extra = LocalExtraColors.current
    val state = when {
        field.showError -> 1
        field.showSuccess -> 2
        else -> 0
    }
    AnimatedContent(
        targetState = state,
        transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
        label = "validationIcon",
    ) { s ->
        when (s) {
            1 -> Icon(Icons.Filled.Error, "Campo con error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 12.dp))
            2 -> Icon(Icons.Filled.CheckCircle, "Campo válido", tint = extra.success, modifier = Modifier.padding(end = 12.dp))
            else -> Spacer(Modifier.width(0.dp))
        }
    }
}

/** Mensaje bajo el campo: el error aparece/desaparece con animación. */
@Composable
fun FieldMessage(error: String?, helper: String? = null) {
    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Row(Modifier.padding(top = 4.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
    if (error == null && helper != null) {
        Text(
            helper,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/** Grupo de chips de selección única o múltiple (tipo de negocio, intereses, categorías). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipGroup(
    label: String?,
    options: List<T>,
    isSelected: (T) -> Boolean,
    onToggle: (T) -> Unit,
    optionLabel: (T) -> String,
    error: String? = null,
    modifier: Modifier = Modifier,
    multi: Boolean = false,
) {
    Column(modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            options.forEach { option ->
                val selected = isSelected(option)
                FilterChip(
                    selected = selected,
                    onClick = { onToggle(option) },
                    label = { Text(optionLabel(option), modifier = Modifier.padding(vertical = 8.dp)) },
                    shape = CircleShape,
                    colors = if (multi) FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) else FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = LocalExtraColors.current.hero,
                        selectedLabelColor = Color.White,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        selectedBorderColor = if (multi) MaterialTheme.colorScheme.primary else LocalExtraColors.current.hero,
                        selectedBorderWidth = 1.5.dp,
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = optionLabel(option) + if (selected) ", seleccionado" else ""
                    },
                )
            }
        }
        FieldMessage(error = error)
    }
}

/** Selector segmentado de Material 3 (tamaño del negocio, modalidad). */
@Composable
fun <T> SegmentedChoice(
    label: String,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    error: String? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        activeBorderColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    ),
                ) { Text(optionLabel(option)) }
            }
        }
        FieldMessage(error = error)
    }
}

/** Botón principal con estado de carga animado. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = CircleShape,
    ) {
        AnimatedContent(targetState = loading, label = "buttonLoading") { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/**
 * Animación de "sacudida" cuando el usuario intenta enviar un formulario inválido.
 * Cada vez que [trigger] cambia (y es > 0) el elemento se sacude horizontalmente.
 */
fun Modifier.shake(trigger: Int): Modifier = composed {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            for (x in listOf(-14f, 14f, -9f, 9f, -4f, 0f)) offset.animateTo(x, tween(45))
        }
    }
    graphicsLayer { translationX = offset.value }
}

/** Ejecuta [onFocusLost] cuando el campo pierde el foco después de haberlo tenido. */
private fun Modifier.onFocusLostOnce(onFocusLost: () -> Unit): Modifier = composed {
    var hadFocus by remember { mutableStateOf(false) }
    onFocusChanged { state ->
        if (state.isFocused) hadFocus = true
        else if (hadFocus) onFocusLost()
    }
}
