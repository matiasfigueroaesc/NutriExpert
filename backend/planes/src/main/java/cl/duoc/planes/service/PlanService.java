
package cl.duoc.planes.service;

import cl.duoc.planes.model.Plan;
import cl.duoc.planes.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    // Listar todos los planes
    public List<Plan> listarTodos() {
        return planRepository.findAll();
    }

    // Listar planes pertenecientes a un sector
    public List<Plan> listarPorSector(Long sectorId) {
        return planRepository.findBySectorId(sectorId);
    }

    // Buscar un plan por su ID
    public Optional<Plan> buscarPorId(Long id) {
        return planRepository.findById(id);
    }

    // Crear un nuevo plan
    public Plan crear(Plan plan) {
        plan.setId(null);
        return planRepository.save(plan);
    }

    // Actualizar un plan existente
    public Optional<Plan> actualizar(Long id, Plan datos) {
        return planRepository.findById(id).map(plan -> {
            plan.setSectorId(datos.getSectorId());
            plan.setNombreTemporada(datos.getNombreTemporada());
            return planRepository.save(plan);
        });
    }

    // Eliminar un plan
    public boolean eliminar(Long id) {
        if (!planRepository.existsById(id)) {
            return false;
        }

        planRepository.deleteById(id);
        return true;
    }
}
