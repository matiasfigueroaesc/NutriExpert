
package cl.duoc.planes.repository;

import cl.duoc.planes.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findBySectorId(Long sectorId);

}
