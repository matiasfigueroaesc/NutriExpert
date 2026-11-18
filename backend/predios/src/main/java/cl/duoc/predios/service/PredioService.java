package cl.duoc.predios.service;

import cl.duoc.predios.model.Predio;
import cl.duoc.predios.repository.PredioRepository;
import cl.duoc.predios.repository.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// La "cocina" de los predios: aquí se decide qué hacer con cada pedido.
// El controller no habla con la base de datos; siempre pasa por aquí.
@Service
public class PredioService {

    private final PredioRepository predioRepository;
    private final SectorRepository sectorRepository;

    // Spring entrega los repositorios solo (inyección por constructor)
    public PredioService(PredioRepository predioRepository, SectorRepository sectorRepository) {
        this.predioRepository = predioRepository;
        this.sectorRepository = sectorRepository;
    }

    // Lista los predios de un usuario. Sin usuarioId, lista todos (útil para probar en Postman).
    public List<Predio> listar(Long usuarioId) {
        if (usuarioId == null) {
            return predioRepository.findAll();
        }
        return predioRepository.findByUsuarioId(usuarioId);
    }

    // Optional = "puede que esté o puede que no". Si no está, el controller responde 404.
    public Optional<Predio> buscarPorId(Long id) {
        return predioRepository.findById(id);
    }

    // Revisa que vengan los datos obligatorios. Devuelve el error o null si está todo bien.
    public String validar(Predio predio) {
        if (predio.getUsuarioId() == null) return "Falta el usuarioId";
        if (predio.getNombre() == null || predio.getNombre().isBlank()) return "Falta el nombre";
        if (predio.getComuna() == null || predio.getComuna().isBlank()) return "Falta la comuna";
        if (predio.getSuperficieHa() == null || predio.getSuperficieHa() <= 0) {
            return "La superficie debe ser mayor a 0";
        }
        return null;
    }

    public Predio crear(Predio predio) {
        predio.setId(null); // el id siempre lo pone la base de datos
        return predioRepository.save(predio);
    }

    // Busca el predio, le copia los datos nuevos y lo guarda.
    public Optional<Predio> actualizar(Long id, Predio datos) {
        return predioRepository.findById(id).map(predio -> {
            predio.setUsuarioId(datos.getUsuarioId());
            predio.setNombre(datos.getNombre());
            predio.setComuna(datos.getComuna());
            predio.setLatitud(datos.getLatitud());
            predio.setLongitud(datos.getLongitud());
            predio.setSuperficieHa(datos.getSuperficieHa());
            return predioRepository.save(predio);
        });
    }

    // Al borrar un predio, primero se borran sus sectores (si no, quedarían "huérfanos").
    // @Transactional: o se borra todo junto, o no se borra nada.
    @Transactional
    public boolean eliminar(Long id) {
        if (!predioRepository.existsById(id)) {
            return false;
        }
        sectorRepository.deleteAll(sectorRepository.findByPredioId(id));
        predioRepository.deleteById(id);
        return true;
    }
}
