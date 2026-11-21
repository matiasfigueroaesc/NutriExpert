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
import cl.duoc.nutriexpert.data.repository.Repositorios
import cl.duoc.nutriexpert.nativo.FotoHelper
import cl.duoc.nutriexpert.seguridad.SesionManager
import cl.duoc.nutriexpert.sync.Sincronizacion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel de la lista "Mis predios".
// La pantalla solo observa "predios" y llama a eliminar(); no toca la base de datos.
//
// Los repositorios llegan por el constructor. En la app se usan los de verdad (valores por defecto);
// en los tests se le pasan unos de mentira (MockK). @JvmOverloads deja que Android lo cree
// con solo el Application, como antes.
class PrediosViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: PredioRepository = Repositorios.predios(application),
    private val authRepository: AuthRepository = AuthRepository(
        AppDatabase.obtener(application).usuarioDao(),
        SesionManager(application)
    ),
    // "Control remoto" para pedir que se sincronice con el servidor apenas haya red
    private val pedirSincronizacion: () -> Unit = { Sincronizacion.pedir(application) }
) : AndroidViewModel(application) {

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

    // Se borra en el teléfono al tiro (la tarjeta desaparece) y después se avisa al servidor
    fun eliminar(predio: PredioEntity) {
        viewModelScope.launch {
            repository.eliminar(predio)
            // También se borra su foto para no dejar archivos huérfanos
            FotoHelper.borrar(getApplication(), predio.fotoUri)
            pedirSincronizacion()
        }
    }

    fun cerrarSesion() = authRepository.cerrarSesion()
}
