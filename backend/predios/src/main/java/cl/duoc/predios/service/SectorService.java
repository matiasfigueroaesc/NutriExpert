package cl.duoc.predios.service;

import cl.duoc.predios.model.Sector;
import cl.duoc.predios.repository.PredioRepository;
import cl.duoc.predios.repository.SectorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;
    private final PredioRepository predioRepository;

    public SectorService(SectorRepository sectorRepository, PredioRepository predioRepository) {
        this.sectorRepository = sectorRepository;
        this.predioRepository = predioRepository;
    }

    // Lista los sectores de un predio. Sin predioId, lista todos.
    public List<Sector> listar(Long predioId) {
        if (predioId == null) {
            return sectorRepository.findAll();
        }
        return sectorRepository.findByPredioId(predioId);
    }

    public Optional<Sector> buscarPorId(Long id) {
        return sectorRepository.findById(id);
    }

    // Devuelve el error o null si está todo bien. El predio tiene que existir.
    public String validar(Sector sector) {
        if (sector.getPredioId() == null) return "Falta el predioId";
        if (!predioRepository.existsById(sector.getPredioId())) return "El predio no existe";
        if (sector.getNombre() == null || sector.getNombre().isBlank()) return "Falta el nombre";
        if (sector.getCultivo() == null || sector.getCultivo().isBlank()) return "Falta el cultivo";
        if (sector.getSuperficieHa() == null || sector.getSuperficieHa() <= 0) {
            return "La superficie debe ser mayor a 0";
        }
        return null;
    }

    public Sector crear(Sector sector) {
        sector.setId(null);
        return sectorRepository.save(sector);
    }

    public Optional<Sector> actualizar(Long id, Sector datos) {
        return sectorRepository.findById(id).map(sector -> {
            sector.setPredioId(datos.getPredioId());
            sector.setNombre(datos.getNombre());
            sector.setCultivo(datos.getCultivo());
            sector.setSuperficieHa(datos.getSuperficieHa());
            return sectorRepository.save(sector);
        });
    }

    public boolean eliminar(Long id) {
        if (!sectorRepository.existsById(id)) {
            return false;
        }
        sectorRepository.deleteById(id);
        return true;
    }
}
