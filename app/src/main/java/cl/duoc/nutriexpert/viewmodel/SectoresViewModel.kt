
package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.data.repository.Repositorios
import cl.duoc.nutriexpert.data.repository.SectorRepository
import cl.duoc.nutriexpert.sync.Sincronizacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel de los sectores de un predio. Igual que PrediosViewModel: el repositorio
// y el "control remoto" de sincronización llegan por el constructor para poder testearlo.
class SectoresViewModel @JvmOverloads constructor(
    application: Application,
    repositorioInicial: SectorRepository = Repositorios.sectores(application),
    private val pedirSincronizacion: () -> Unit = { Sincronizacion.pedir(application) }
) : AndroidViewModel(application) {

    private val _sectores =
        MutableStateFlow<List<SectorEntity>>(emptyList())

    val sectores: StateFlow<List<SectorEntity>> = _sectores

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private var repository: SectorRepository? = null
    private var cargaJob: Job? = null

    // Se conecta al repositorio al crearse (Room + microservicio)
    init {
        configurarRepositorio(repositorioInicial)
    }

    // Busca un sector para editarlo (null si no existe)
    suspend fun obtenerSector(id: Int): SectorEntity? = repository?.obtenerPorId(id)

    fun configurarRepositorio(repo: SectorRepository) {
        cargaJob?.cancel()
        repository = repo
        _sectores.value = emptyList()
        _error.value = null
    }

    fun cargarSectores(predioId: Int) {
        val repo = repository

        if (repo == null) {
            _error.value = "El repositorio de sectores no está conectado"
            return
        }

        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            try {
                repo.obtenerPorPredio(predioId).collect { lista ->
                    _sectores.value = lista
                    _error.value = null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar sectores"
            }
        }
    }

    fun guardarSector(
        sector: SectorEntity,
        onResultado: (Boolean) -> Unit
    ) {
        val repo = repository

        if (repo == null) {
            _error.value = "El repositorio de sectores no está conectado"
            onResultado(false)
            return
        }

        viewModelScope.launch {
            _guardando.value = true

            try {
                repo.guardar(sector)
                _error.value = null
                pedirSincronizacion()
                onResultado(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al guardar sector"
                onResultado(false)
            } finally {
                _guardando.value = false
            }
        }
    }

    fun eliminarSector(
        sector: SectorEntity,
        onResultado: (Boolean) -> Unit
    ) {
        val repo = repository

        if (repo == null) {
            _error.value = "El repositorio de sectores no está conectado"
            onResultado(false)
            return
        }

        viewModelScope.launch {
            try {
                repo.eliminar(sector)
                _error.value = null
                pedirSincronizacion()
                onResultado(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al eliminar sector"
                onResultado(false)
            }
        }
    }
}
