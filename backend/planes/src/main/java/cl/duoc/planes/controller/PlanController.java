
package cl.duoc.planes.controller;

import cl.duoc.planes.model.Plan;
import cl.duoc.planes.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // GET: listar planes de un sector
    @GetMapping
    public List<Plan> listarPorSector(@RequestParam Long sectorId) {
        return planService.listarPorSector(sectorId);
    }

    // GET: buscar un plan por ID
    @GetMapping("/{id}")
    public ResponseEntity<Plan> buscarPorId(@PathVariable Long id) {
        return planService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: crear un plan
    @PostMapping
    public ResponseEntity<Plan> crear(@RequestBody Plan plan) {
        Plan nuevo = planService.crear(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT: actualizar un plan
    @PutMapping("/{id}")
    public ResponseEntity<Plan> actualizar(
            @PathVariable Long id,
            @RequestBody Plan datos) {

        return planService.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE: eliminar un plan
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (planService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
