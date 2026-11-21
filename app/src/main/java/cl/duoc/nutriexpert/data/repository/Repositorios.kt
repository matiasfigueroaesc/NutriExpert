package cl.duoc.nutriexpert.data.repository

import android.content.Context
import cl.duoc.nutriexpert.data.local.AppDatabase
import cl.duoc.nutriexpert.data.remote.RetrofitClient
import cl.duoc.nutriexpert.data.remote.planes.RetrofitPlanesClient
import cl.duoc.nutriexpert.seguridad.SesionManager

// "Armador" de repositorios: junta Room, Retrofit y la sesión en un solo lugar,
// para que los ViewModels y el SyncWorker no tengan que saber cómo se arman.
object Repositorios {

    fun predios(context: Context): PredioRepository {
        val db = AppDatabase.obtener(context)
        return PredioRepository(
            dao = db.predioDao(),
            sectorDao = db.sectorDao(),
            api = RetrofitClient.predioApi,
            usuarioId = SesionManager(context).usuarioId() ?: 0 // 0 = nadie, no trae nada
        )
    }

    fun sectores(context: Context): SectorRepository {
        val db = AppDatabase.obtener(context)
        return SectorRepository(
            dao = db.sectorDao(),
            predioDao = db.predioDao(),
            api = RetrofitClient.sectorApi
        )
    }

    fun planes(context: Context): PlanRepository =
        PlanRepository(
            planDao = AppDatabase.obtener(context).planDao(),
            api = RetrofitPlanesClient.api
        )
}
