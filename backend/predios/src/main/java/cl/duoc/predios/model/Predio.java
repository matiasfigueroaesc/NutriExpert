package cl.duoc.predios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// La "ficha" de un predio. Cada objeto Predio es una fila de la tabla "predios".
// Los nombres de los campos son los mismos que viajan en el JSON (contrato del equipo).
@Entity
@Table(name = "predios")
public class Predio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la base de datos pone el id sola
    private Long id;

    private Long usuarioId;      // dueño del predio (id del microservicio usuarios)
    private String nombre;       // ej: "Predio Los Aromos"
    private String comuna;       // ej: "Quillota"
    private Double latitud;      // puede venir vacío si no se usó el GPS
    private Double longitud;
    private Double superficieHa; // superficie total en hectáreas

    // Constructor vacío: lo necesita JPA para armar el objeto al leer de la tabla
    public Predio() {
    }

    public Predio(Long usuarioId, String nombre, String comuna,
                  Double latitud, Double longitud, Double superficieHa) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.comuna = comuna;
        this.latitud = latitud;
        this.longitud = longitud;
        this.superficieHa = superficieHa;
    }

    // Getters y setters: Spring los usa para pasar de objeto a JSON y de JSON a objeto

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public Double getSuperficieHa() { return superficieHa; }
    public void setSuperficieHa(Double superficieHa) { this.superficieHa = superficieHa; }
}
