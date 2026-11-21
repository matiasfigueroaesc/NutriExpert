
package cl.duoc.planes.repository;

import cl.duoc.planes.model.Plan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PlanRepositoryTest {

    @Autowired
    private PlanRepository planRepository;

    @Test
    void guardarYBuscarPlanPorSector() {

        // Crear un plan de prueba
        Plan plan = new Plan(1L, "Primavera 2026");

        // Guardar en H2
        planRepository.save(plan);

        // Buscar los planes del sector 1
        var resultados = planRepository.findBySectorId(1L);

        // Comprobar que se guardó correctamente
        assertEquals(1, resultados.size());
        assertEquals("Primavera 2026",
                resultados.get(0).getNombreTemporada());
    }
}
