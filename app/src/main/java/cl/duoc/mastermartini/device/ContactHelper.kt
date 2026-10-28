package cl.duoc.mastermartini.device

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * RECURSO NATIVO 2 — Teléfono, WhatsApp y hoja de compartir del sistema.
 * Todo se hace con Intents implícitos:
 * - ACTION_DIAL abre el marcador con el número escrito, sin llamar
 *   (no requiere el permiso peligroso CALL_PHONE).
 * - WhatsApp: se intenta la app; si no está instalada se abre wa.me en el navegador.
 * - ACTION_SEND abre el selector nativo para compartir texto.
 * Cada método devuelve false si no hay ninguna app capaz de manejarlo.
 */
object ContactHelper {

    fun dial(context: Context, phone: String): Boolean =
        launch(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))

    fun whatsApp(context: Context, phone: String, message: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        val uri = Uri.parse("https://wa.me/$digits?text=${Uri.encode(message)}")
        val appIntent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.whatsapp")
        val businessIntent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.whatsapp.w4b")
        return launch(context, appIntent) || launch(context, businessIntent) ||
            launch(context, Intent(Intent.ACTION_VIEW, uri))
    }

    fun share(context: Context, subject: String, text: String): Boolean {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        return launch(context, Intent.createChooser(send, "Compartir con…"))
    }

    private fun launch(context: Context, intent: Intent): Boolean = try {
        if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
