package cl.duoc.mastermartini.ui.screens.services

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.ServiceType
import cl.duoc.mastermartini.device.rememberNotificationPermission
import cl.duoc.mastermartini.domain.validation.Validators
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.ChipGroup
import cl.duoc.mastermartini.ui.components.DateField
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.MMTextField
import cl.duoc.mastermartini.ui.components.Pill
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.ScreenTitle
import cl.duoc.mastermartini.ui.components.SegmentedChoice
import cl.duoc.mastermartini.ui.components.shake
import cl.duoc.mastermartini.ui.theme.LocalExtraColors

@Composable
fun ServicesScreen(
    onBookAdvisory: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    snackbar: SnackbarHostState,
    vm: ServicesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val askNotifications = rememberNotificationPermission()
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Servicios", Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        val tabs: List<ServiceType?> = listOf(null) + ServiceType.entries
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            tabs.forEachIndexed { i, t ->
                SegmentedButton(
                    selected = s.filter == t,
                    onClick = { vm.onFilter(t) },
                    shape = SegmentedButtonDefaults.itemShape(i, tabs.size),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) { Text(t?.label ?: "Todos", maxLines = 1, style = MaterialTheme.typography.labelMedium) }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(s.services, key = { it.id }) { item ->
                ServiceCard(
                    item = item,
                    reserved = item.id in s.reservedIds,
                    modifier = Modifier.animateItem(),
                    onEnroll = { vm.askEnroll(item) },
                    onBook = { onBookAdvisory(item.id) },
                    onCourse = { item.learnId?.let(onOpenCourse) },
                )
            }
        }
    }

    s.confirming?.let { item ->
        AlertDialog(
            onDismissRequest = vm::dismissEnroll,
            icon = { Icon(Icons.Outlined.EventAvailable, null) },
            title = { Text("¿Inscribirte?") },
            text = { Text("${item.title}\n${item.dateLabel}\n${item.detail}") },
            confirmButton = { Button(onClick = { askNotifications { vm.confirmEnroll() } }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = vm::dismissEnroll) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun ServiceCard(
    item: ServiceItem,
    reserved: Boolean,
    modifier: Modifier,
    onEnroll: () -> Unit,
    onBook: () -> Unit,
    onCourse: () -> Unit,
) {
    val extra = LocalExtraColors.current
    MMCard(modifier) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (item.type) {
                    ServiceType.EVENT -> Pill(item.badge, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                    ServiceType.ADVISORY -> Pill(item.badge, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface)
                    ServiceType.COURSE -> Pill(item.badge, extra.successContainer, extra.success)
                }
                Spacer(Modifier.weight(1f))
                val side = when {
                    item.type == ServiceType.EVENT -> {
                        val left = item.availableSeats - if (reserved) 1 else 0
                        "$left de ${item.totalSeats} cupos"
                    }
                    item.hasCertificate -> "Con certificado"
                    else -> null
                }
                side?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(
                listOfNotNull(item.dateLabel, item.detail).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (item.type) {
                ServiceType.EVENT ->
                    if (reserved) {
                        OutlinedButton(
                            onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = extra.success)
                            Spacer(Modifier.width(6.dp))
                            Text("Inscrita/o", color = extra.success)
                        }
                    } else {
                        Button(onClick = onEnroll, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                            Text("Inscribirme")
                        }
                    }
                ServiceType.ADVISORY -> OutlinedButton(
                    onClick = onBook, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                ) { Text("Reservar hora") }
                ServiceType.COURSE -> {
                    val target = item.progress ?: 0f
                    // La barra se "llena" con animación al aparecer la tarjeta.
                    var started by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { started = true }
                    val progress by animateFloatAsState(if (started) target else 0f, tween(900), label = "courseProgress")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.weight(1f).height(8.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("${(progress * 100).toInt()} %", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = onCourse, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    ) { Text(if (target > 0f) "Continuar curso" else "Comenzar curso") }
                }
            }
        }
    }
}

/** Formulario de reserva de asesoría técnica. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onBack: () -> Unit,
    vm: BookingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val askNotifications = rememberNotificationPermission()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar asesoría") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                s.service?.detail ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DateField(
                label = "Fecha",
                valueLabel = s.dateLabel,
                selectedMillis = s.dateMillis,
                error = s.dateError,
                onDateSelected = vm::onDate,
                isSelectable = { Validators.futureWorkingDate(it).isValid },
            )
            ChipGroup(
                label = "Horario",
                options = TIME_SLOTS,
                isSelected = { it == s.time },
                onToggle = vm::onTime,
                optionLabel = { "$it h" },
                error = s.timeError,
            )
            SegmentedChoice(
                label = "Modalidad",
                options = Modality.entries,
                selected = s.modality,
                onSelect = vm::onModality,
                optionLabel = { it.label },
                error = s.modalityError,
            )
            // La dirección solo se pide si la asesoría es en el local (aparece animada).
            AnimatedVisibility(
                visible = s.modality == Modality.ON_SITE,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                MMTextField(
                    "Dirección del local", s.address, vm::onAddress, Icons.Outlined.Home,
                    placeholder = "Ej: Av. Matta 1234", onFocusLost = vm::onAddressBlur,
                )
            }
            ChipGroup(
                label = "Tema",
                options = ADVISORY_TOPICS,
                isSelected = { it == s.topic },
                onToggle = vm::onTopic,
                optionLabel = { it },
                error = s.topicError,
            )
            MMTextField(
                "¿Qué necesitas resolver?", s.description, vm::onDescription, Icons.Outlined.Description,
                singleLine = false, minLines = 3, imeAction = ImeAction.Default,
                helper = "${s.description.value.length}/250", onFocusLost = vm::onDescriptionBlur,
            )
            PrimaryButton(
                "Confirmar reserva",
                onClick = { askNotifications { vm.submit() } },
                loading = s.loading,
                icon = Icons.Outlined.EventAvailable,
                modifier = Modifier.shake(s.failedAttempts),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (s.confirmed) {
        AlertDialog(
            onDismissRequest = onBack,
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = LocalExtraColors.current.success) },
            title = { Text("¡Reserva confirmada!") },
            text = {
                Text(
                    "${s.dateLabel} · ${s.time} h · ${s.modality?.label}.\nLa verás en Mi cuenta → Mis reservas." +
                        if (s.notified) "" else "\n(Activa las notificaciones para recibir recordatorios.)"
                )
            },
            confirmButton = { TextButton(onClick = onBack) { Text("Listo") } },
        )
    }
}
