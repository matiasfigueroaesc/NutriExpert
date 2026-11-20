
package cl.duoc.nutriexpert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.PlanEntity
import cl.duoc.nutriexpert.data.repository.PlanRepository
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

    fun cargarPlanes(sectorId: Int) {
        viewModelScope.launch {
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudieron cargar los planes"
                )
            }
        }
    }

    fun guardarPlan(
        sectorId: Int,
        nombreTemporada: String
    ) {
        if (nombreTemporada.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Ingresa un nombre para la temporada"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                guardando = true,
                error = null
            )

            try {
                repository.guardarPlan(
                    sectorId = sectorId,
                    nombreTemporada = nombreTemporada.trim()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo guardar el plan"
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    guardando = false
                )
            }
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            try {
                repository.sincronizarPendientes()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo completar la sincronización"
                )
            }
        }
    }
}
