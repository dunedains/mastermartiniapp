package cl.duoc.mastermartini.data.model

/**
 * Modelos de dominio de la app. Son clases simples (sin dependencias de Android)
 * que usan las pantallas. Los datos de contenido son ficticios (MVP académico).
 */

enum class BusinessType(val label: String) {
    PASTELERIA("Pastelería"),
    PANADERIA("Panadería"),
    CHOCOLATERIA("Chocolatería"),
    HELADERIA("Heladería"),
    RESTAURANTE("Restaurante / Casino");

    companion object {
        fun fromName(name: String?): BusinessType? = entries.firstOrNull { it.name == name }
    }
}

enum class BusinessSize(val label: String) {
    PEQUENO("Pequeño"),
    MEDIANO("Mediano"),
    GRANDE("Grande");

    companion object {
        fun fromName(name: String?): BusinessSize? = entries.firstOrNull { it.name == name }
    }
}

/** Temas que el usuario puede marcar como interés al registrarse. */
val LEARNING_INTERESTS = listOf(
    "Chocolatería", "Pastelería de vitrina", "Masas de hoja", "Heladería", "Galletas", "Panadería"
)

/** Usuario ya autenticado (sin datos sensibles como el hash de la contraseña). */
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val businessType: BusinessType,
    val businessSize: BusinessSize,
    val interests: List<String>,
) {
    val initials: String
        get() = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
    val firstName: String get() = name.trim().split(" ").firstOrNull().orEmpty()
}

// ---------- Aprende ----------

enum class LearnKind(val label: String) { VIDEO("Videos"), RECIPE("Recetas"), COURSE("Cursos") }

enum class Difficulty(val label: String) { EASY("Fácil"), MEDIUM("Medio"), HARD("Avanzado") }

data class LearnItem(
    val id: String,
    val kind: LearnKind,
    val title: String,
    val category: String,
    val duration: String,
    val difficulty: Difficulty,
    val description: String,
    val steps: List<String>,
    /** ids de productos del catálogo que se usan en este contenido. */
    val productIds: List<String>,
    /** Color de la miniatura (placeholder hasta tener fotos reales). */
    val thumbColor: Long,
    val author: String = "Chef Master Martini",
    val isNew: Boolean = false,
)

// ---------- Catálogo ----------

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val formats: List<String>,
    val description: String,
)

/** Producto agregado a la cotización en curso. */
data class QuoteLine(
    val productId: String,
    val productName: String,
    val format: String,
    val quantity: Int,
)

data class SentQuote(
    val id: Long,
    val code: String,
    val itemsSummary: String,
    val itemCount: Int,
    val address: String,
    val comuna: String,
    val deliveryDate: String,
    val createdAt: Long,
)

// ---------- Servicios ----------

enum class ServiceType(val label: String) { COURSE("Cursos"), ADVISORY("Asesorías"), EVENT("Eventos") }

data class ServiceItem(
    val id: String,
    val type: ServiceType,
    val title: String,
    val badge: String,
    val detail: String,
    val dateLabel: String? = null,
    val takenSeats: Int = 0,
    val totalSeats: Int = 0,
    val progress: Float? = null,
    val hasCertificate: Boolean = false,
    /** Para cursos: id del contenido en "Aprende" que se abre al continuar. */
    val learnId: String? = null,
) {
    val availableSeats: Int get() = totalSeats - takenSeats
}

data class Reservation(
    val id: Long,
    val serviceId: String,
    val title: String,
    val dateLabel: String,
    val modality: String,
    val createdAt: Long,
)

// ---------- Pedidos (datos de ejemplo) ----------

data class OrderStep(val label: String, val date: String?, val done: Boolean, val current: Boolean)

data class Order(
    val code: String,
    val productCount: Int,
    val delivered: Boolean,
    val deliveredDate: String?,
    val steps: List<OrderStep>,
)
