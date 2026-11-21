
package cl.duoc.nutriexpert.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Base de datos local de NutriExpert.
// Al agregar una tabla nueva, se incrementa la version.

@Database(
    entities = [
        PredioEntity::class,
        CultivoEntity::class,
        FertilizanteEntity::class,
        EtapaEntity::class,
        SectorEntity::class,
        AnalisisEntity::class,
        UsuarioEntity::class,
        PlanEntity::class
    ],
    // 3: tabla usuarios y dueño (usuarioId) de cada predio
    // 4: predios y sectores guardan remoteId, sincronizado y eliminado (sincronización con el backend)
    // 5: tabla planes (con temporada, fechas y sincronización)
    version = 5,
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
    abstract fun planDao(): PlanDao

    companion object {

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

                        CoroutineScope(
                            SupervisorJob() + Dispatchers.IO
                        ).launch {
                            CatalogoInicial.cargar(db)
                        }
                    }
            }
        }
    }
}


