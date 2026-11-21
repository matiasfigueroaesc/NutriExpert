package cl.duoc.predios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// La "ficha" de un sector: una parte del predio con un solo cultivo.
@Entity
@Table(name = "sectores")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long predioId;       // a qué predio pertenece
    private String nombre;       // ej: "Sector norte"
    private String cultivo;      // ej: "palto"
    private Double superficieHa; // hectáreas del sector

    public Sector() {
    }

    public Sector(Long predioId, String nombre, String cultivo, Double superficieHa) {
        this.predioId = predioId;
        this.nombre = nombre;
        this.cultivo = cultivo;
        this.superficieHa = superficieHa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPredioId() { return predioId; }
    public void setPredioId(Long predioId) { this.predioId = predioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCultivo() { return cultivo; }
    public void setCultivo(String cultivo) { this.cultivo = cultivo; }

    public Double getSuperficieHa() { return superficieHa; }
    public void setSuperficieHa(Double superficieHa) { this.superficieHa = superficieHa; }
}
