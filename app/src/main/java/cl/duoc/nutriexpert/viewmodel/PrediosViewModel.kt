package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.AppDatabase
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.PredioRepository
import cl.duoc.nutriexpert.nativo.FotoHelper
import cl.duoc.nutriexpert.seguridad.SesionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel de la lista "Mis predios".
// La pantalla solo observa "predios" y llama a eliminar(); no toca la base de datos.
class PrediosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val sesion = SesionManager(application)
    private val authRepository = AuthRepository(db.usuarioDao(), sesion)

    // Solo los predios del usuario que inició sesión (0 = nadie, no trae nada)
    private val repository = PredioRepository(db.predioDao(), sesion.usuarioId() ?: 0)

    // Lista que la pantalla observa. Se actualiza sola cuando cambia la tabla.
    val predios: StateFlow<List<PredioEntity>> = repository.predios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nombre del usuario para el saludo de la pantalla
    var nombreUsuario by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            nombreUsuario = authRepository.usuarioActual()?.nombre.orEmpty()
        }
    }

    fun eliminar(predio: PredioEntity) {
        viewModelScope.launch {
            repository.eliminar(predio)
            // También se borra su foto para no dejar archivos huérfanos
            FotoHelper.borrar(getApplication(), predio.fotoUri)
        }
    }

    fun cerrarSesion() = authRepository.cerrarSesion()
}
