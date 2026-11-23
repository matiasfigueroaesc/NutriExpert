
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
        PlanEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun predioDao(): PredioDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun fertilizanteDao(): FertilizanteDao
    abstract fun etapaDao(): EtapaDao
    abstract fun sectorDao(): SectorDao
    abstract fun analisisDao(): AnalisisDao
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    
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



