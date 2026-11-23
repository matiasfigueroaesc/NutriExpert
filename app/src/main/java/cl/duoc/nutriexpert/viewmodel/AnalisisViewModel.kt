
package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import cl.duoc.nutriexpert.data.local.AppDatabase
import cl.duoc.nutriexpert.data.repository.AnalisisRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// El repositorio llega por el constructor para poder testearlo (igual que SectoresViewModel).
class AnalisisViewModel @JvmOverloads constructor(
    application: Application,
    repositorioInicial: AnalisisRepository =
        AnalisisRepository(AppDatabase.obtener(application).analisisDao())
) : AndroidViewModel(application) {

    private val _analisis =
        MutableStateFlow<List<AnalisisEntity>>(emptyList())

    val analisis: StateFlow<List<AnalisisEntity>> = _analisis

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private var repository: AnalisisRepository? = null
    private var cargaJob: Job? = null

    // Se conecta solo a Room al crearse (igual que PrediosViewModel)
    init {
        configurarRepositorio(repositorioInicial)
    }

    fun configurarRepositorio(repo: AnalisisRepository) {
        cargaJob?.cancel()
        repository = repo
        _analisis.value = emptyList()
        _error.value = null
    }

    fun cargarAnalisis(sectorId: Int) {
        val repo = repository

        if (repo == null) {
            _error.value = "El repositorio de análisis no está conectado"
            return
        }

        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            try {
                repo.obtenerPorSector(sectorId).collect { lista ->
                    _analisis.value = lista
                    _error.value = null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar análisis"
            }
        }
    }

    fun guardarAnalisis(
        analisis: AnalisisEntity,
        onResultado: (Boolean) -> Unit
    ) {
        val repo = repository

        if (repo == null) {
            _error.value = "El repositorio de análisis no está conectado"
            onResultado(false)
            return
        }

        viewModelScope.launch {
            _guardando.value = true

            try {
                repo.guardar(analisis)
                _error.value = null
                onResultado(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al guardar análisis"
                onResultado(false)
            } finally {
                _guardando.value = false
            }
        }
    }

    fun eliminarAnalisis(
        analisis: AnalisisEntity,
        onResultado: (Boolean) -> Unit
    ) {
        val repo = repository

        if (repo == null) {
            _error.value = "El repositorio de análisis no está conectado"
            onResultado(false)
            return
        }

        viewModelScope.launch {
            try {
                repo.eliminar(analisis)
                _error.value = null
                onResultado(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al eliminar análisis"
                onResultado(false)
            }
        }
    }
}
