package cl.duoc.nutriexpert.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// Le dice a WorkManager CUÁNDO ejecutar el SyncWorker.
object Sincronizacion {

    // Condición: solo con internet. Sin red, WorkManager espera solo a que vuelva.
    private val conRed = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // Sincronizar apenas haya red. Se llama después de guardar o borrar algo.
    fun pedir(context: Context) {
        val trabajo = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(conRed)
            .build()
        // APPEND_OR_REPLACE: si ya hay una sincronización andando, esta se pone en la fila
        WorkManager.getInstance(context)
            .enqueueUniqueWork("sincronizar-ahora", ExistingWorkPolicy.APPEND_OR_REPLACE, trabajo)
    }

    // Revisión cada 15 minutos (lo mínimo que permite Android), por si algo quedó pendiente.
    fun programarPeriodica(context: Context) {
        val trabajo = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(conRed)
            .build()
        // KEEP: si ya estaba programada, no la duplica
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("sincronizar-periodica", ExistingPeriodicWorkPolicy.KEEP, trabajo)
    }
}
