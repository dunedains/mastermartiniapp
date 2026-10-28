package cl.duoc.mastermartini.domain.validation

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Resultado de validar un campo: válido, o inválido con un mensaje para mostrar bajo el campo. */
data class ValidationResult(val isValid: Boolean, val error: String? = null) {
    companion object {
        val Ok = ValidationResult(true)
        fun fail(message: String) = ValidationResult(false, message)
    }
}

/**
 * Estado de un campo de texto en un formulario.
 * - [touched]: el usuario ya interactuó con el campo (no mostramos errores antes de eso).
 * - [error]: mensaje a mostrar; null si es válido.
 */
data class FieldState(
    val value: String = "",
    val error: String? = null,
    val touched: Boolean = false,
) {
    val showError: Boolean get() = touched && error != null
    val showSuccess: Boolean get() = touched && error == null && value.isNotBlank()

    /** Nuevo valor + validación inmediata (el error solo se ve si el campo ya fue "tocado"). */
    fun update(newValue: String, rule: (String) -> ValidationResult) =
        copy(value = newValue, error = rule(newValue).error)

    /** Marca el campo como tocado y lo valida (al perder foco o al enviar). */
    fun touch(rule: (String) -> ValidationResult) =
        copy(touched = true, error = rule(value).error)
}

/**
 * LÓGICA DE VALIDACIÓN CENTRALIZADA.
 * Funciones puras, sin dependencias de Android ni de Compose: las usan todos los
 * ViewModels y se prueban con tests unitarios (ver src/test).
 * Si mañana Master Martini cambia una regla (ej. largo mínimo de contraseña),
 * se cambia solo aquí.
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val NAME_REGEX = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' ]+$")

    fun name(value: String): ValidationResult {
        val v = value.trim()
        return when {
            v.isEmpty() -> ValidationResult.fail("Ingresa tu nombre")
            !NAME_REGEX.matches(v) -> ValidationResult.fail("Usa solo letras y espacios")
            v.split(" ").filter { it.isNotBlank() }.size < 2 -> ValidationResult.fail("Ingresa nombre y apellido")
            v.length > 60 -> ValidationResult.fail("Máximo 60 caracteres")
            else -> ValidationResult.Ok
        }
    }

    fun email(value: String): ValidationResult {
        val v = value.trim()
        return when {
            v.isEmpty() -> ValidationResult.fail("Ingresa tu correo")
            !EMAIL_REGEX.matches(v) -> ValidationResult.fail("Formato inválido (ej: correo@ejemplo.cl)")
            else -> ValidationResult.Ok
        }
    }

    /** Celular chileno: +56 9 XXXX XXXX (se aceptan espacios y guiones). */
    fun phone(value: String): ValidationResult {
        val digits = value.filter { it.isDigit() }
        val local = if (digits.startsWith("56")) digits.drop(2) else digits
        return when {
            value.isBlank() -> ValidationResult.fail("Ingresa tu teléfono")
            value.any { !(it.isDigit() || it == '+' || it == ' ' || it == '-') } ->
                ValidationResult.fail("Solo números, espacios y +")
            local.length != 9 || !local.startsWith("9") ->
                ValidationResult.fail("Debe ser un celular válido: +56 9 1234 5678")
            else -> ValidationResult.Ok
        }
    }

    fun password(value: String): ValidationResult = when {
        value.isEmpty() -> ValidationResult.fail("Crea una contraseña")
        value.length < 8 -> ValidationResult.fail("Mínimo 8 caracteres")
        value.none { it.isDigit() } -> ValidationResult.fail("Incluye al menos un número")
        value.none { it.isLetter() } -> ValidationResult.fail("Incluye al menos una letra")
        else -> ValidationResult.Ok
    }

    fun passwordConfirmation(password: String, confirmation: String): ValidationResult = when {
        confirmation.isEmpty() -> ValidationResult.fail("Repite tu contraseña")
        password != confirmation -> ValidationResult.fail("Las contraseñas no coinciden")
        else -> ValidationResult.Ok
    }

    /** Para el login solo pedimos que no esté vacía (no revelamos reglas). */
    fun loginPassword(value: String): ValidationResult =
        if (value.isEmpty()) ValidationResult.fail("Ingresa tu contraseña") else ValidationResult.Ok

    fun <T> selected(value: T?, message: String): ValidationResult =
        if (value == null) ValidationResult.fail(message) else ValidationResult.Ok

    fun atLeast(items: Collection<*>, min: Int, message: String): ValidationResult =
        if (items.size < min) ValidationResult.fail(message) else ValidationResult.Ok

    fun acceptedTerms(accepted: Boolean): ValidationResult =
        if (!accepted) ValidationResult.fail("Debes aceptar para crear tu cuenta") else ValidationResult.Ok

    fun quantity(value: String, max: Int = 999): ValidationResult {
        val n = value.trim().toIntOrNull()
        return when {
            value.isBlank() -> ValidationResult.fail("Ingresa una cantidad")
            n == null -> ValidationResult.fail("Debe ser un número entero")
            n < 1 -> ValidationResult.fail("Mínimo 1 unidad")
            n > max -> ValidationResult.fail("Máximo $max unidades")
            else -> ValidationResult.Ok
        }
    }

    fun address(value: String): ValidationResult {
        val v = value.trim()
        return when {
            v.isEmpty() -> ValidationResult.fail("Ingresa la dirección de despacho")
            v.length < 6 -> ValidationResult.fail("Dirección demasiado corta")
            v.none { it.isDigit() } -> ValidationResult.fail("Incluye el número de la calle")
            v.length > 120 -> ValidationResult.fail("Máximo 120 caracteres")
            else -> ValidationResult.Ok
        }
    }

    fun required(value: String, message: String, min: Int = 2): ValidationResult = when {
        value.isBlank() -> ValidationResult.fail(message)
        value.trim().length < min -> ValidationResult.fail("Mínimo $min caracteres")
        else -> ValidationResult.Ok
    }

    /** El valor debe ser una de las opciones permitidas (ej. comuna de la lista). */
    fun oneOf(value: String, options: List<String>, message: String): ValidationResult = when {
        value.isBlank() -> ValidationResult.fail(message)
        options.none { it.equals(value.trim(), ignoreCase = true) } -> ValidationResult.fail("Elige una opción de la lista")
        else -> ValidationResult.Ok
    }

    fun optionalText(value: String, max: Int = 300): ValidationResult =
        if (value.length > max) ValidationResult.fail("Máximo $max caracteres (${value.length}/$max)")
        else ValidationResult.Ok

    /**
     * Fecha de despacho o asesoría: desde mañana, máximo 60 días, de lunes a sábado.
     * @param millisUtc valor que entrega el DatePicker de Material 3 (medianoche UTC).
     */
    fun futureWorkingDate(millisUtc: Long?, today: LocalDate = LocalDate.now()): ValidationResult {
        if (millisUtc == null) return ValidationResult.fail("Selecciona una fecha")
        val date = Instant.ofEpochMilli(millisUtc).atZone(ZoneOffset.UTC).toLocalDate()
        return when {
            !date.isAfter(today) -> ValidationResult.fail("Elige una fecha desde mañana")
            date.isAfter(today.plusDays(60)) -> ValidationResult.fail("Máximo 60 días de anticipación")
            date.dayOfWeek == DayOfWeek.SUNDAY -> ValidationResult.fail("No atendemos domingos")
            else -> ValidationResult.Ok
        }
    }
}
