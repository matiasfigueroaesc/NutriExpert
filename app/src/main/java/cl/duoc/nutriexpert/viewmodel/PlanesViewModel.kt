package cl.duoc.nutriexpert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.PlanEntity
import cl.duoc.nutriexpert.data.repository.PlanRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlanesUiState(
    val planes: List<PlanEntity> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null
)

class PlanesViewModel(
    private val repository: PlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanesUiState())
    val uiState: StateFlow<PlanesUiState> = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    fun cargarPlanes(sectorId: Int) {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null
            )

            try {
                repository.obtenerPlanes(sectorId).collect { planes ->
                    _uiState.value = _uiState.value.copy(
                        planes = planes,
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudieron cargar los planes"
                )
            }
        }
    }

    fun guardarPlan(sectorId: Int, nombreTemporada: String, fechaInicio: String = "", fechaTermino: String = "") {
        if (nombreTemporada.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Ingresa un nombre para la temporada"
            )
            return
        }

        ejecutarOperacion {
            repository.guardarPlan(sectorId, nombreTemporada, fechaInicio, fechaTermino)
        }
    }

    fun editarPlan(id: Int, nuevoNombre: String) {
        if (nuevoNombre.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre no puede estar vacío"
            )
            return
        }

        ejecutarOperacion {
            repository.editarPlan(id, nuevoNombre)
        }
    }

    fun eliminarPlan(id: Int) {
        ejecutarOperacion {
            repository.eliminarPlan(id)
        }
    }

    fun sincronizar() {
        ejecutarOperacion {
            repository.sincronizarPendientes()
        }
    }

    private fun ejecutarOperacion(
        operacion: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                guardando = true,
                error = null
            )

            try {
                operacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo completar la operación"
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    guardando = false
                )
            }
        }
    }
}
