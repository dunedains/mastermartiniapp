package cl.duoc.mastermartini.device

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Pide el permiso de notificaciones SOLO cuando el usuario hace algo que lo justifica
 * (inscribirse, reservar, enviar cotización) y no al abrir la app.
 * Uso:
 *   val askNotifications = rememberNotificationPermission()
 *   askNotifications { granted -> vm.submit() }
 * La acción se ejecuta siempre: si el usuario rechaza, la app sigue funcionando sin notificar.
 */
@Composable
fun rememberNotificationPermission(): ((Boolean) -> Unit) -> Unit {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pending?.invoke(granted)
        pending = null
    }
    return remember(launcher) {
        { action: (Boolean) -> Unit ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                action(true)
            } else {
                pending = action
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
