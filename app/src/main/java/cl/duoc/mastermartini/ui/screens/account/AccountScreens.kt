package cl.duoc.mastermartini.ui.screens.account

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.MasterMartiniApp
import cl.duoc.mastermartini.data.model.BusinessSize
import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.LEARNING_INTERESTS
import cl.duoc.mastermartini.data.model.Order
import cl.duoc.mastermartini.device.ContactHelper
import cl.duoc.mastermartini.device.rememberNotificationPermission
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.Avatar
import cl.duoc.mastermartini.ui.components.ChipGroup
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.MMDivider
import cl.duoc.mastermartini.ui.components.MMTextField
import cl.duoc.mastermartini.ui.components.Pill
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.ScreenTitle
import cl.duoc.mastermartini.ui.components.SegmentedChoice
import cl.duoc.mastermartini.ui.components.shake
import cl.duoc.mastermartini.ui.theme.LocalExtraColors
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    onEditProfile: () -> Unit,
    onGoToCatalog: () -> Unit,
    onLoggedOut: () -> Unit,
    snackbar: SnackbarHostState,
    vm: AccountViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val askNotifications = rememberNotificationPermission()
    val notifier = remember { (context.applicationContext as MasterMartiniApp).container.notifications }
    var notificationsOn by remember { mutableStateOf(notifier.hasPermission()) }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    fun toast(msg: String) = scope.launch { snackbar.showSnackbar(msg) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenTitle("Mi cuenta")
        s.user?.let { u ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(u.initials)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(u.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${u.businessType.label} · Negocio ${u.businessSize.label.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onEditProfile) { Icon(Icons.Outlined.Edit, "Editar perfil") }
            }
        }

        s.orders.forEach { order ->
            if (!order.delivered) OrderTrackingCard(order) else MMCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Pedido #${order.code}", style = MaterialTheme.typography.titleSmall)
                        Text("Entregado · ${order.deliveredDate}", style = MaterialTheme.typography.bodySmall, color = LocalExtraColors.current.success, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { vm.repeatOrder(onGoToCatalog) },
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        shape = MaterialTheme.shapes.small,
                    ) { Text("Repetir pedido") }
                }
            }
        }

        if (s.quotes.isNotEmpty()) {
            MMCard(Modifier.animateContentSize()) {
                Column(Modifier.padding(16.dp)) {
                    Pill("Cotizaciones enviadas", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary)
                    s.quotes.forEachIndexed { i, q ->
                        if (i > 0) MMDivider()
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Text("${q.code} · ${q.itemCount} producto${if (q.itemCount == 1) "" else "s"}", style = MaterialTheme.typography.titleSmall)
                            Text("${q.comuna} · despacho ${q.deliveryDate.lowercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        MMCard(Modifier.animateContentSize()) {
            Column(Modifier.padding(16.dp)) {
                Pill("Mis reservas", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary)
                if (s.reservations.isEmpty()) {
                    Text("Aún no tienes reservas. Revisa Servicios.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                }
                s.reservations.forEachIndexed { i, r ->
                    if (i > 0) MMDivider()
                    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(r.title, style = MaterialTheme.typography.titleSmall)
                            Text("${r.dateLabel} · ${r.modality}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { vm.cancelReservation(r.id) }) { Text("Cancelar", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }

        // Recurso nativo: llamada y WhatsApp a ventas.
        MMCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("¿Dudas con un pedido?", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { if (!ContactHelper.dial(context, s.salesPhone)) toast("No hay app de teléfono") },
                        modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small,
                    ) {
                        Icon(Icons.Outlined.Call, null); Spacer(Modifier.width(6.dp)); Text("Llamar")
                    }
                    OutlinedButton(
                        onClick = {
                            val ok = ContactHelper.whatsApp(context, s.salesPhone, "Hola, soy ${s.user?.name.orEmpty()} y tengo una consulta.")
                            if (!ok) toast("No pudimos abrir WhatsApp")
                        },
                        modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small,
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Chat, null); Spacer(Modifier.width(6.dp)); Text("WhatsApp")
                    }
                }
            }
        }

        MMCard {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SettingSwitch("Texto grande", s.largeText, vm::setLargeText)
                MMDivider()
                SettingSwitch("Alto contraste", s.highContrast, vm::setHighContrast)
                MMDivider()
                SettingSwitch("Notificaciones", notificationsOn) { wanted ->
                    if (wanted) askNotifications { granted ->
                        notificationsOn = granted
                        if (!granted) toast("Puedes activarlas desde los ajustes del teléfono")
                    } else toast("Desactívalas desde los ajustes del teléfono")
                }
            }
        }

        TextButton(
            onClick = { vm.logout(); onLoggedOut() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, null)
            Spacer(Modifier.width(6.dp))
            Text("Cerrar sesión")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                stateDescription = if (checked) "Activado" else "Desactivado"
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label == "Notificaciones") {
            Icon(Icons.Outlined.NotificationsActive, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** Seguimiento del pedido con línea de tiempo; el paso actual "late" para llamar la atención. */
@Composable
private fun OrderTrackingCard(order: Order) {
    val extra = LocalExtraColors.current
    val pulse = rememberInfiniteTransition(label = "orderPulse")
    val scale by pulse.animateFloat(1f, 1.25f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "stepScale")
    MMCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row {
                Text("Pedido #${order.code}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("${order.productCount} productos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            order.steps.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        step.done -> Box(
                            Modifier.size(22.dp).background(extra.success, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(14.dp)) }
                        step.current -> Box(
                            Modifier.size(22.dp).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Box(Modifier.size(10.dp).scale(scale).background(MaterialTheme.colorScheme.primary, CircleShape)) }
                        else -> Box(Modifier.size(22.dp).border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        step.label + (step.date?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (step.current) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            step.current -> MaterialTheme.colorScheme.primary
                            step.done -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

// ===================== Editar perfil =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    vm: EditProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(s.saved) { if (s.saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (!s.loaded) return@Scaffold
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            MMTextField("Nombre", s.name, vm::onName, Icons.Outlined.Person)
            MMTextField("Teléfono", s.phone, vm::onPhone, Icons.Outlined.Phone, keyboardType = KeyboardType.Phone)
            ChipGroup(
                label = "Tipo de negocio",
                options = BusinessType.entries,
                isSelected = { it == s.businessType },
                onToggle = vm::onType,
                optionLabel = { it.label },
            )
            SegmentedChoice("Tamaño del negocio", BusinessSize.entries, s.businessSize, vm::onSize, { it.label })
            ChipGroup(
                label = "Intereses",
                options = LEARNING_INTERESTS,
                isSelected = { it in s.interests },
                onToggle = vm::onInterest,
                optionLabel = { it },
                error = s.interestsError,
                multi = true,
            )
            PrimaryButton(
                "Guardar cambios", vm::save, loading = s.saving, icon = Icons.Outlined.Save,
                modifier = Modifier.shake(s.failedAttempts),
            )
        }
    }
}
