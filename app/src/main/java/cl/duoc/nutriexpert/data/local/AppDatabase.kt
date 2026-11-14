package cl.duoc.nutriexpert.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Base de datos local de la app. Aquí se registran todas las tablas (entities).
// Al agregar una tabla nueva hay que subir "version" en 1.
@Database(
    entities = [
        PredioEntity::class,
        CultivoEntity::class,
        FertilizanteEntity::class,
        EtapaEntity::class,
        SectorEntity::class,
        AnalisisEntity::class,
        UsuarioEntity::class
    ],
    // 3: se agregó la tabla usuarios y el dueño (usuarioId) de cada predio
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun predioDao(): PredioDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun fertilizanteDao(): FertilizanteDao
    abstract fun etapaDao(): EtapaDao
    abstract fun sectorDao(): SectorDao
    abstract fun analisisDao(): AnalisisDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        // Una sola instancia para toda la app
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutriexpert.db"
                )
                    // Mientras desarrollamos: si cambia la versión, se borra y se crea de nuevo
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { db ->
                        INSTANCIA = db
                        // Carga o actualiza el catálogo en segundo plano al abrir la app
                        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                            CatalogoInicial.cargar(db)
                        }
                    }
            }
        }
    }
}