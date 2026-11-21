package cl.duoc.predios.repository;

import cl.duoc.predios.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    // SELECT * FROM sectores WHERE predio_id = ?
    List<Sector> findByPredioId(Long predioId);
}
