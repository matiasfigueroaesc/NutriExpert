package cl.duoc.usuarios.repository;

import cl.duoc.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// El "bodeguero" de los usuarios. JpaRepository ya trae guardar, buscar por id, listar y borrar.
// Solo declaramos lo que falta; Spring arma la consulta leyendo el nombre del método.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // SELECT * FROM usuarios WHERE email = ?
    Optional<Usuario> findByEmail(String email);

    // ¿Ya existe una cuenta con este correo?
    boolean existsByEmail(String email);
}
