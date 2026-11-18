package cl.duoc.predios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de partida del microservicio: al ejecutarlo, levanta el servidor en el puerto 8081.
@SpringBootApplication
public class PrediosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrediosApplication.class, args);
    }
}
