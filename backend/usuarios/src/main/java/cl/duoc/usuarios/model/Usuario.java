package cl.duoc.usuarios.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// La "ficha" de un usuario. Cada objeto Usuario es una fila de la tabla "usuarios".
// Los nombres de los campos son los mismos que viajan en el JSON (contrato del equipo).
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la base de datos pone el id sola
    private Long id;

    @Column(nullable = false)
    private String nombre;     // ej: "Fabián"

    @Column(nullable = false, unique = true)
    private String email;      // único: no puede haber dos cuentas con el mismo correo

    // Se guarda encriptada con BCrypt, nunca en texto plano.
    // WRITE_ONLY: se puede RECIBIR en el JSON (registro y login), pero NUNCA se envía en la respuesta.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    // Constructor vacío: lo necesita JPA para armar el objeto al leer de la tabla
    public Usuario() {
    }

    public Usuario(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    // Getters y setters: Spring los usa para pasar de objeto a JSON y de JSON a objeto

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
