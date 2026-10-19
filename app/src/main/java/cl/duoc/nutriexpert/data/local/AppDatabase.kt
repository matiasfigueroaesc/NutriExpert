package cl.duoc.nutriexpert.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Base de datos local de la app. Aquí se registran todas las tablas (entities).
@Database(entities = [PredioEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun predioDao(): PredioDao

    companion object {
        // Una sola instancia para toda la app
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriexpert.db"
                ).build()
                INSTANCIA = db
                db
            }
        }
    }
}
