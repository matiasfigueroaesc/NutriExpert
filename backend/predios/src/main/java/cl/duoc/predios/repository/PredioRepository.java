package cl.duoc.predios.repository;

import cl.duoc.predios.model.Predio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// El "bodeguero" de los predios. JpaRepository ya trae guardar, buscar por id, listar y borrar.
// Solo declaramos lo que falta; Spring arma la consulta leyendo el nombre del método.
public interface PredioRepository extends JpaRepository<Predio, Long> {

    // SELECT * FROM predios WHERE usuario_id = ?
    List<Predio> findByUsuarioId(Long usuarioId);
}
