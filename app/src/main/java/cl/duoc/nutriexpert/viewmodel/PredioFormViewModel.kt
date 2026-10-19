package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.AppDatabase
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.repository.PredioRepository
import cl.duoc.nutriexpert.utils.ErroresPredio
import cl.duoc.nutriexpert.utils.validarPredio
import kotlinx.coroutines.launch

// Estado del formulario: lo que escribe el usuario y los errores de cada campo
data class PredioFormState(
    val id: Int = 0,
    val nombre: String = "",
    val comuna: String = "",
    val hectareas: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUri: String? = null,
    val errores: ErroresPredio = ErroresPredio(),
    val guardado: Boolean = false   // true cuando se guardó y hay que volver a la lista
)

// ViewModel del formulario "Nuevo predio" / "Editar predio".
// La pantalla muestra "estado" y avisa cada cambio; la validación está en Validaciones.kt.
class PredioFormViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PredioRepository(
        AppDatabase.obtener(application).predioDao()
    )

    var estado by mutableStateOf(PredioFormState())
        private set

    // Si el id es distinto de 0, carga el predio para editarlo
    fun cargar(id: Int) {
        if (id == 0 || estado.id == id) return
        viewModelScope.launch {
            val predio = repository.obtenerPorId(id) ?: return@launch
            estado = PredioFormState(
                id = predio.id,
                nombre = predio.nombre,
                comuna = predio.comuna,
                hectareas = predio.hectareas.toString(),
                latitud = predio.latitud,
                longitud = predio.longitud,
                fotoUri = predio.fotoUri
            )
        }
    }

    // Al escribir se borra el error de ese campo
    fun onNombreChange(valor: String) {
        estado = estado.copy(nombre = valor, errores = estado.errores.copy(nombre = null))
    }

    fun onComunaChange(valor: String) {
        estado = estado.copy(comuna = valor, errores = estado.errores.copy(comuna = null))
    }

    fun onHectareasChange(valor: String) {
        estado = estado.copy(hectareas = valor, errores = estado.errores.copy(hectareas = null))
    }

    // Para la parte de recursos nativos (GPS y cámara)
    fun onUbicacionChange(latitud: Double, longitud: Double) {
        estado = estado.copy(latitud = latitud, longitud = longitud)
    }

    // uri = null cuando el usuario quita la foto
    fun onFotoChange(uri: String?) {
        estado = estado.copy(fotoUri = uri)
    }

    fun guardar() {
        val errores = validarPredio(estado.nombre, estado.comuna, estado.hectareas)
        if (errores.hayErrores()) {
            estado = estado.copy(errores = errores)
            return
        }
        viewModelScope.launch {
            repository.guardar(
                PredioEntity(
                    id = estado.id,
                    nombre = estado.nombre.trim(),
                    comuna = estado.comuna.trim(),
                    hectareas = estado.hectareas.replace(",", ".").toDouble(),
                    latitud = estado.latitud,
                    longitud = estado.longitud,
                    fotoUri = estado.fotoUri
                )
            )
            estado = estado.copy(guardado = true)
        }
    }
}
