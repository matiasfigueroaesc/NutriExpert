package cl.duoc.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Entrega el encriptador de contraseñas a quien lo pida (el service).
// Solo usamos BCrypt; Spring Security completo NO está activado (no hay login de Spring ni filtros).
@Configuration
public class SeguridadConfig {

    // BCrypt: cada contraseña se guarda con su propia sal y es lenta de adivinar a propósito.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
