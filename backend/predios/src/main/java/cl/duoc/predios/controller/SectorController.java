package cl.duoc.predios.controller;

import cl.duoc.predios.model.Sector;
import cl.duoc.predios.service.SectorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// El "mesón de atención" de los sectores. Funciona igual que PredioController.
@RestController
@RequestMapping("/api/sectores")
public class SectorController {

    private final SectorService service;

    public SectorController(SectorService service) {
        this.service = service;
    }

    // GET /api/sectores?predioId=1
    @GetMapping
    public List<Sector> listar(@RequestParam(required = false) Long predioId) {
        return service.listar(predioId);
    }

    // GET /api/sectores/5
    @GetMapping("/{id}")
    public ResponseEntity<Sector> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)                      // 200 con el sector
                .orElse(ResponseEntity.notFound().build());   // 404 si no existe
    }

    // POST /api/sectores   (el sector viene en el cuerpo, en JSON)
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Sector sector) {
        String error = service.validar(sector);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error)); // 400
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(sector)); // 201
    }

    // PUT /api/sectores/5
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Sector sector) {
        String error = service.validar(sector);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error));
        }
        return service.actualizar(id, sector)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/sectores/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build(); // 204: borrado, no hay nada que devolver
        }
        return ResponseEntity.notFound().build();
    }
}
