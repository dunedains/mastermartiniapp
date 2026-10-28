package cl.duoc.mastermartini.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tablas de Room (SQLite local). */

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    /** Nunca guardamos la contraseña en texto plano: solo su hash SHA-256 con sal. */
    val passwordHash: String,
    val salt: String,
    val businessType: String,
    val businessSize: String,
    /** Intereses separados por "|" */
    val interests: String,
    val acceptedTerms: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Ítems de la cotización en curso (carrito). Uno por producto + formato. */
@Entity(tableName = "quote_items", primaryKeys = ["userId", "productId", "format"])
data class QuoteItemEntity(
    val userId: Long,
    val productId: String,
    val productName: String,
    val format: String,
    val quantity: Int,
)

/** Cotizaciones ya enviadas a ventas. */
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val itemsSummary: String,
    val itemCount: Int,
    val contactName: String,
    val address: String,
    val comuna: String,
    val deliveryDate: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Inscripciones a eventos/cursos y reservas de asesorías. */
@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val serviceId: String,
    val title: String,
    val dateLabel: String,
    val modality: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
