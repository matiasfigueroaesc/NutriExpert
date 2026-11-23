package cl.duoc.usuarios.controller;

import cl.duoc.usuarios.model.Usuario;
import cl.duoc.usuarios.service.UsuarioService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// El "mesón de atención" de los usuarios: recibe el pedido HTTP, se lo pasa al service
// y responde con el código que corresponde (200, 201, 204, 400, 401 o 404).
// La contraseña nunca aparece en las respuestas (ver @JsonProperty en Usuario).
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final String YA_EXISTE = "Ya existe una cuenta con ese correo";

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // POST /api/usuarios/registro   { "nombre": "...", "email": "...", "password": "..." }
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        String error = service.validarRegistro(usuario);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error)); // 400
        }
        if (service.emailRegistrado(usuario.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", YA_EXISTE)); // 400
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(usuario)); // 201
        } catch (DataIntegrityViolationException e) {
            // Dos registros con el mismo correo llegaron al mismo tiempo: la base de datos frenó el segundo
            return ResponseEntity.badRequest().body(Map.of("mensaje", YA_EXISTE));
        }
    }

    // POST /api/usuarios/login   { "email": "...", "password": "..." }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario datos) {
        if (datos.getEmail() == null || datos.getEmail().isBlank()
                || datos.getPassword() == null || datos.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Faltan el email o la contraseña")); // 400
        }
        return service.login(datos.getEmail(), datos.getPassword())
                .<ResponseEntity<?>>map(ResponseEntity::ok)                          // 200 con el usuario
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)               // 401
                        .body(Map.of("mensaje", "Correo o contraseña incorrectos")));
    }

    // GET /api/usuarios/5
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)                      // 200 con el usuario
                .orElse(ResponseEntity.notFound().build());   // 404 si no existe
    }

    // PUT /api/usuarios/5   { "nombre": "Nuevo nombre" }   (solo cambia el nombre)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Usuario datos) {
        String error = service.validarNombre(datos.getNombre());
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", error));
        }
        return service.actualizar(id, datos)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/usuarios/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build(); // 204: borrado, no hay nada que devolver
        }
        return ResponseEntity.notFound().build();
    }
}
