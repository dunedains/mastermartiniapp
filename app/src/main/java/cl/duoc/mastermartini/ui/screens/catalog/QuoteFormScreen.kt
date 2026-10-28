package cl.duoc.mastermartini.ui.screens.catalog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.MasterMartiniApp
import cl.duoc.mastermartini.device.ContactHelper
import cl.duoc.mastermartini.device.rememberNotificationPermission
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.DateField
import cl.duoc.mastermartini.ui.components.EmptyState
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.MMDivider
import cl.duoc.mastermartini.ui.components.MMTextField
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.shake
import cl.duoc.mastermartini.ui.theme.LocalExtraColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteFormScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: QuoteFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val lines by vm.lines.collectAsStateWithLifecycle()
    val askNotifications = rememberNotificationPermission()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar cotización") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Resumen de productos (la tarjeta se redimensiona con animación al quitar ítems).
            MMCard(Modifier.animateContentSize()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Productos (${lines.size})", style = MaterialTheme.typography.titleSmall)
                    if (lines.isEmpty()) EmptyState("Tu cotización está vacía. Agrega productos desde el catálogo.")
                    lines.forEachIndexed { i, line ->
                        if (i > 0) MMDivider()
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(line.productName, style = MaterialTheme.typography.bodyLarge)
                                Text("${line.quantity} × ${line.format}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { vm.removeLine(line.productId) }) {
                                Icon(Icons.Outlined.Close, "Quitar ${line.productName}")
                            }
                        }
                    }
                }
            }

            Text("Datos de despacho", style = MaterialTheme.typography.titleMedium)
            MMTextField("Nombre de contacto", s.contactName, vm::onContact, Icons.Outlined.Person, onFocusLost = vm::onContactBlur)
            MMTextField(
                "Dirección", s.address, vm::onAddress, Icons.Outlined.Home,
                placeholder = "Ej: Av. Matta 1234, local 2", onFocusLost = vm::onAddressBlur,
            )
            ComunaField(s.comuna, vm::onComuna)
            DateField(
                label = "Fecha de despacho deseada",
                valueLabel = s.dateLabel,
                selectedMillis = s.dateMillis,
                error = s.dateError,
                onDateSelected = vm::onDate,
                isSelectable = { Validators.futureWorkingDate(it).isValid },
            )
            MMTextField(
                "Comentarios (opcional)", s.notes, vm::onNotes, Icons.AutoMirrored.Outlined.Notes,
                singleLine = false, minLines = 3, imeAction = ImeAction.Default,
                helper = "${s.notes.value.length}/300",
            )
            PrimaryButton(
                "Enviar a ventas",
                onClick = { askNotifications { vm.submit() } },
                loading = s.loading,
                enabled = lines.isNotEmpty(),
                icon = Icons.AutoMirrored.Filled.Send,
                modifier = Modifier.shake(s.failedAttempts),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    s.sentCode?.let { code ->
        val phone = (context.applicationContext as MasterMartiniApp).container.content.salesPhone()
        AlertDialog(
            onDismissRequest = onDone,
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = LocalExtraColors.current.success) },
            title = { Text("¡Cotización $code enviada!") },
            text = {
                Text(
                    "Ventas revisará tu solicitud y te contactará." +
                        if (s.notified) " Te dejamos una notificación con el detalle." else
                            " (Activa las notificaciones para recibir avisos.)"
                )
            },
            confirmButton = { TextButton(onClick = onDone) { Text("Listo") } },
            dismissButton = {
                OutlinedButton(onClick = {
                    ContactHelper.whatsApp(context, phone, "Hola, envié la cotización $code desde la app:\n${s.sentSummary}")
                }) {
                    Icon(Icons.AutoMirrored.Outlined.Chat, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Avisar por WhatsApp")
                }
            },
        )
    }
}

/** Comuna con lista desplegable que filtra mientras se escribe. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComunaField(field: FieldState, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = COMUNAS.filter { field.value.isBlank() || it.contains(field.value.trim(), ignoreCase = true) }
    ExposedDropdownMenuBox(expanded = expanded && options.isNotEmpty(), onExpandedChange = { expanded = it }) {
        MMTextField(
            label = "Comuna",
            field = field,
            onValueChange = { onChange(it); expanded = true },
            leadingIcon = Icons.Outlined.LocationCity,
            placeholder = "Escribe o elige",
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
            trailingAction = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded && options.isNotEmpty(), onDismissRequest = { expanded = false }) {
            options.forEach { comuna ->
                DropdownMenuItem(text = { Text(comuna) }, onClick = { onChange(comuna); expanded = false })
            }
        }
    }
}
