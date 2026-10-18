package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.AppDatabase
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.repository.PredioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel de la lista "Mis predios".
// La pantalla solo observa "predios" y llama a eliminar(); no toca la base de datos.
class PrediosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PredioRepository(
        AppDatabase.obtener(application).predioDao()
    )

    // Lista que la pantalla observa. Se actualiza sola cuando cambia la tabla.
    val predios: StateFlow<List<PredioEntity>> = repository.predios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun eliminar(predio: PredioEntity) {
        viewModelScope.launch {
            repository.eliminar(predio)
        }
    }
}
