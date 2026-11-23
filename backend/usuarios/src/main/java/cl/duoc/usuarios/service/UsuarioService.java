package cl.duoc.usuarios.service;

import cl.duoc.usuarios.model.Usuario;
import cl.duoc.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

// La "cocina" de los usuarios: aquí se decide qué hacer con cada pedido.
// El controller no habla con la base de datos; siempre pasa por aquí.
@Service
public class UsuarioService {

    // algo@algo.dominio (el mismo criterio que la app)
    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");

    // Límite de BCrypt: si se pasa, Spring Security lanza una excepción
    private static final int MAX_BYTES_BCRYPT = 72;

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    // Spring entrega el repositorio y el encriptador solos (inyección por constructor)
    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    // Revisa los datos del registro. Devuelve el error o null si está todo bien.
    // Las reglas son las mismas que valida la app, para que el usuario vea el mismo mensaje.
    public String validarRegistro(Usuario usuario) {
        String errorNombre = validarNombre(usuario.getNombre());
        if (errorNombre != null) return errorNombre;

        String email = usuario.getEmail();
        if (email == null || email.isBlank()) return "Falta el email";
        if (!EMAIL.matcher(email.trim()).matches()) return "El email no es válido";

        String password = usuario.getPassword();
        if (password == null || password.isBlank()) return "Falta la contraseña";
        if (password.length() < 8) return "La contraseña debe tener al menos 8 caracteres";
        // BCrypt acepta como máximo 72 bytes (una letra con tilde ocupa 2)
        if (demasiadoLarga(password)) return "La contraseña es demasiado larga";
        boolean tieneLetra = password.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = password.chars().anyMatch(Character::isDigit);
        if (!tieneLetra || !tieneNumero) return "La contraseña debe tener letras y números";

        return null;
    }

    public String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return "Falta el nombre";
        if (nombre.trim().length() < 2) return "El nombre debe tener al menos 2 caracteres";
        return null;
    }

    public boolean emailRegistrado(String email) {
        return repository.existsByEmail(normalizar(email));
    }

    // Guarda el usuario con la contraseña encriptada. Nunca se guarda en texto plano.
    public Usuario registrar(Usuario usuario) {
        usuario.setId(null); // el id siempre lo pone la base de datos
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setEmail(normalizar(usuario.getEmail()));
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        return repository.save(usuario);
    }

    // Busca por correo y compara la contraseña escrita con la encriptada (matches).
    // Optional vacío = correo o contraseña incorrectos (no se dice cuál, a propósito).
    public Optional<Usuario> login(String email, String password) {
        if (email == null || password == null || demasiadoLarga(password)) return Optional.empty();
        return repository.findByEmail(normalizar(email))
                .filter(usuario -> encoder.matches(password, usuario.getPassword()));
    }

    // Optional = "puede que esté o puede que no". Si no está, el controller responde 404.
    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Desde el perfil solo se cambia el nombre. El correo y la contraseña no se tocan aquí.
    public Optional<Usuario> actualizar(Long id, Usuario datos) {
        return repository.findById(id).map(usuario -> {
            usuario.setNombre(datos.getNombre().trim());
            return repository.save(usuario);
        });
    }

    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private boolean demasiadoLarga(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_BCRYPT;
    }

    // " Fabian@Gmail.com " y "fabian@gmail.com" son la misma cuenta
    private String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
