package cl.duoc.nutriexpert.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.duoc.nutriexpert.data.repository.Repositorios
import cl.duoc.nutriexpert.seguridad.SesionManager
import kotlinx.coroutines.flow.first

// Trabajador en segundo plano: lleva al servidor lo que quedó pendiente en Room
// y trae lo que el servidor tiene y el teléfono no.
// Es como el repartidor que pasa cada cierto rato: si encuentra la calle cortada (sin red),
// vuelve más tarde. Android lo despierta solo, aunque la app esté cerrada.
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Sin sesión no hay nada que sincronizar
        if (!SesionManager(applicationContext).haySesion()) return Result.success()

        val predios = Repositorios.predios(applicationContext)
        val sectores = Repositorios.sectores(applicationContext)

        // 1. Subir: primero los predios, porque los sectores necesitan el id del predio en el servidor
        val prediosOk = predios.sincronizarPendientes()
        val sectoresOk = sectores.sincronizarPendientes()
        // Para sumar los planes: val planesOk = Repositorios.planes(applicationContext).sincronizarPendientes()

        // 2. Bajar: lo que está en el servidor y no en el teléfono
        val descargaOk = predios.descargar()
        for (predio in predios.predios.first()) {
            if (predio.remoteId != null) sectores.descargar(predio.id)
        }

        // retry = "inténtalo de nuevo más tarde" (WorkManager espera cada vez un poco más)
        return if (prediosOk && sectoresOk && descargaOk) Result.success() else Result.retry()
    }
}
