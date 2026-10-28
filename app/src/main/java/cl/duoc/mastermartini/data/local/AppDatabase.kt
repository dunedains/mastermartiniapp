package cl.duoc.mastermartini.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, QuoteItemEntity::class, QuoteEntity::class, ReservationEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun quoteDao(): QuoteDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "master_martini.db",
            )
                // En etapa MVP: si cambia el esquema se recrea la BD.
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
