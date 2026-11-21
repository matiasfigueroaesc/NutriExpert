package cl.duoc.predios.service;

import cl.duoc.predios.model.Predio;
import cl.duoc.predios.model.Sector;
import cl.duoc.predios.repository.SectorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba el service de verdad, con la base H2 en memoria (no se finge nada).
@SpringBootTest
class PredioServiceTest {

    @Autowired
    private PredioService predioService;

    @Autowired
    private SectorService sectorService;

    @Autowired
    private SectorRepository sectorRepository;

    @Test
    void crearPredioConSectorYBorrarlo_borraTambienElSector() {
        // 1. Crear un predio y un sector dentro de él
        Predio predio = predioService.crear(new Predio(1L, "Los Aromos", "Quillota", null, null, 10.0));
        Sector sector = sectorService.crear(new Sector(predio.getId(), "Norte", "palto", 4.0));
        assertNotNull(predio.getId());
        assertEquals(1, sectorService.listar(predio.getId()).size());

        // 2. Borrar el predio
        assertTrue(predioService.eliminar(predio.getId()));

        // 3. Ya no existe el predio ni su sector
        assertTrue(predioService.buscarPorId(predio.getId()).isEmpty());
        assertFalse(sectorRepository.existsById(sector.getId()));
    }

    @Test
    void eliminarPredioQueNoExiste_devuelveFalse() {
        assertFalse(predioService.eliminar(9999L));
    }

    @Test
    void listarPorUsuario_soloTraeLosDeEseUsuario() {
        predioService.crear(new Predio(50L, "Uno", "Rancagua", null, null, 1.0));
        predioService.crear(new Predio(50L, "Dos", "Rancagua", null, null, 2.0));
        predioService.crear(new Predio(51L, "Otro", "Talca", null, null, 3.0));

        assertEquals(2, predioService.listar(50L).size());
        assertEquals(1, predioService.listar(51L).size());
    }

    @Test
    void actualizar_cambiaLosDatos() {
        Predio predio = predioService.crear(new Predio(1L, "Viejo", "Ovalle", null, null, 5.0));
        Predio datos = new Predio(1L, "Nuevo", "Ovalle", -30.6, -71.2, 6.0);

        Predio actualizado = predioService.actualizar(predio.getId(), datos).orElseThrow();

        assertEquals("Nuevo", actualizado.getNombre());
        assertEquals(6.0, actualizado.getSuperficieHa());
        assertTrue(predioService.actualizar(9999L, datos).isEmpty());
    }

    @Test
    void validar_avisaCadaDatoQueFalta() {
        assertEquals("Falta el usuarioId", predioService.validar(new Predio(null, "A", "B", null, null, 1.0)));
        assertEquals("Falta el nombre", predioService.validar(new Predio(1L, " ", "B", null, null, 1.0)));
        assertEquals("Falta la comuna", predioService.validar(new Predio(1L, "A", null, null, null, 1.0)));
        assertEquals("La superficie debe ser mayor a 0", predioService.validar(new Predio(1L, "A", "B", null, null, 0.0)));
        assertNull(predioService.validar(new Predio(1L, "A", "B", null, null, 1.0)));
    }

    @Test
    void validarSector_elPredioTieneQueExistir() {
        Predio predio = predioService.crear(new Predio(1L, "P", "Quillota", null, null, 10.0));

        assertEquals("Falta el predioId", sectorService.validar(new Sector(null, "S", "palto", 1.0)));
        assertEquals("El predio no existe", sectorService.validar(new Sector(9999L, "S", "palto", 1.0)));
        assertEquals("Falta el nombre", sectorService.validar(new Sector(predio.getId(), "", "palto", 1.0)));
        assertEquals("Falta el cultivo", sectorService.validar(new Sector(predio.getId(), "S", null, 1.0)));
        assertEquals("La superficie debe ser mayor a 0", sectorService.validar(new Sector(predio.getId(), "S", "palto", -1.0)));
        assertNull(sectorService.validar(new Sector(predio.getId(), "S", "palto", 1.0)));
    }
}
