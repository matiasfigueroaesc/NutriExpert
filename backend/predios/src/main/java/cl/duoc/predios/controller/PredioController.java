package cl.duoc.predios.controller;

import cl.duoc.predios.model.Predio;
import cl.duoc.predios.service.PredioService;
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

// El "mesón de atención" de los predios: recibe el pedido HTTP, se lo pasa al service
// y responde con el código que corresponde (200, 201, 204, 400 o 404).
@RestController
@RequestMapping("/api/predios")
public class PredioController {

    private final PredioService service;

    public PredioController(PredioService service) {
        this.service = service;
    }

    // GET /api/predios?usuarioId=1
    @GetMapping
    public List<Predio> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    // GET /api/predios/5
    @GetMapping("/{id}")
    public ResponseEntity<Predio> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)                      // 200 con el predio
                .orElse(ResponseEntity.notFound().build());   // 404 si no existe
    }

    // POST /api/predios   (el predio viene en el cuerpo, en JSON)
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Predio predio) {
        String error = service.validar(predio);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error)); // 400
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(predio)); // 201
    }

    // PUT /api/predios/5
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Predio predio) {
        String error = service.validar(predio);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error));
        }
        return service.actualizar(id, predio)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/predios/5   (también borra sus sectores)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build(); // 204: borrado, no hay nada que devolver
        }
        return ResponseEntity.notFound().build();
    }
}
