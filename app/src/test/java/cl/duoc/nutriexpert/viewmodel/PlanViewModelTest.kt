package cl.duoc.nutriexpert.viewmodel

import cl.duoc.nutriexpert.domain.model.CatalogoEjemplo
import cl.duoc.nutriexpert.domain.model.Nutriente
import org.junit.Assert.*
import org.junit.Test

class PlanViewModelTest {

    private fun crearViewModel() = PlanViewModel()

    @Test
    fun estadoInicialTieneValoresEsperados() {
        val vm = crearViewModel()

        assertEquals("5", vm.estado.hectareas)
        assertEquals(CatalogoEjemplo.TOMATE, vm.estado.cultivo)
        assertEquals(1000.0, vm.estado.volumenL, 0.001)
        assertFalse(vm.estado.mostrarAjustes)
    }

    @Test
    fun aceptaHectareasConComaDecimal() {
        val vm = crearViewModel()

        vm.onHectareasChange("7,5")

        assertEquals(7.5, vm.hectareasNumero(), 0.001)
        assertTrue(vm.validarMeta())
    }

    @Test
    fun rechazaHectareasEnCero() {
        val vm = crearViewModel()

        vm.onHectareasChange("0")

        assertFalse(vm.validarMeta())
        assertNotNull(vm.estado.errorHectareas)
    }

    @Test
    fun rechazaHectareasNegativas() {
        val vm = crearViewModel()

        vm.onHectareasChange("-2")

        assertFalse(vm.validarMeta())
        assertNotNull(vm.estado.errorHectareas)
    }

    @Test
    fun rechazaHectareasNoNumericas() {
        val vm = crearViewModel()

        vm.onHectareasChange("abc")

        assertFalse(vm.validarMeta())
        assertEquals(0.0, vm.hectareasNumero(), 0.001)
    }

    @Test
    fun usarSectorActualizaSuperficie() {
        val vm = crearViewModel()

        vm.usarSector(CatalogoEjemplo.TOMATE.id, 8.5)

        assertEquals("8.5", vm.estado.hectareas)
        assertEquals(CatalogoEjemplo.TOMATE, vm.estado.cultivo)
    }

    @Test
    fun cambiarCultivoActualizaDemandaYCurvas() {
        val vm = crearViewModel()
        val cultivo = vm.cultivos.first()

        vm.onCultivoChange(cultivo)

        assertEquals(cultivo, vm.estado.cultivo)
        assertEquals(textoDe(cultivo.demanda), vm.estado.demanda)
        assertEquals(curvasDe(cultivo), vm.estado.curvas)
    }

    @Test
    fun rechazaDemandaNegativa() {
        val vm = crearViewModel()
        val nutriente = Nutriente.entries.first()

        vm.onDemandaChange(nutriente, "-10")

        assertFalse(vm.validarMeta())
        assertTrue(nutriente in vm.estado.erroresDemanda)
    }

    @Test
    fun aceptaDemandaCero() {
        val vm = crearViewModel()
        val nutriente = Nutriente.entries.first()

        vm.onDemandaChange(nutriente, "0")

        assertTrue(vm.validarMeta())
        assertFalse(nutriente in vm.estado.erroresDemanda)
    }

    @Test
    fun deseleccionarTodosLosFertilizantesMuestraError() {
        val vm = crearViewModel()
        val seleccionados = vm.estado.seleccionados.toList()

        seleccionados.forEach { vm.onFertilizanteToggle(it) }

        assertTrue(vm.estado.seleccionados.isEmpty())
        assertFalse(vm.validarFertilizantes())
        assertNotNull(vm.estado.errorFertilizantes)
    }

    @Test
    fun seleccionarFertilizantePermiteContinuar() {
        val vm = crearViewModel()
        val seleccionados = vm.estado.seleccionados.toList()

        seleccionados.forEach { vm.onFertilizanteToggle(it) }
        vm.onFertilizanteToggle(vm.fertilizantes.first().id)

        assertTrue(vm.validarFertilizantes())
        assertNull(vm.estado.errorFertilizantes)
    }

    @Test
    fun cambiaVisibilidadDeAjustesYDetalle() {
        val vm = crearViewModel()

        vm.onMostrarAjustesChange(true)
        vm.onMostrarDetalleChange(true)

        assertTrue(vm.estado.mostrarAjustes)
        assertTrue(vm.estado.mostrarDetalle)
    }

    @Test
    fun cambiarVolumenActualizaEstado() {
        val vm = crearViewModel()

        vm.onVolumenChange(1500.0)

        assertEquals(1500.0, vm.estado.volumenL, 0.001)
    }

    @Test
    fun cambiarConcentracionMaximaActualizaEstado() {
        val vm = crearViewModel()

        vm.onCmaxChange(2.5)

        assertEquals(2.5, vm.estado.cmaxPct, 0.001)
    }

    @Test
    fun cambiarEtapaActualizaEstado() {
        val vm = crearViewModel()

        vm.onEtapaEstanqueChange(0)

        assertEquals(0, vm.estado.etapaEstanque)
    }

    @Test
    fun metaDevuelveNutrientes() {
        val vm = crearViewModel()

        val resultado = vm.meta()

        assertTrue(resultado.isNotEmpty())
        assertTrue(resultado.values.all { it >= 0.0 })
    }

    @Test
    fun etapasCoincidenConCultivo() {
        val vm = crearViewModel()

        assertEquals(vm.estado.cultivo.etapas.size, vm.etapas().size)
    }

    @Test
    fun resultadoCalculaDosis() {
        val vm = crearViewModel()

        val resultado = vm.resultado()

        assertNotNull(resultado)
    }
}
