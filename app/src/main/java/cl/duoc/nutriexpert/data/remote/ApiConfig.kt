package cl.duoc.nutriexpert.data.remote

// Direcciones de los microservicios, en un solo lugar.
// 10.0.2.2 es "el PC" visto desde el emulador. Cuando la EC2 esté lista,
// se cambia SERVIDOR por la Elastic IP (ej: "http://54.123.45.67") y listo.
object ApiConfig {
    private const val SERVIDOR = "http://10.0.2.2"

    const val URL_USUARIOS = "$SERVIDOR:8080/"   // microservicio usuarios (Fabián)
    const val URL_PREDIOS = "$SERVIDOR:8081/"    // microservicio predios (predios y sectores)
    const val URL_PLANES = "$SERVIDOR:8082/"     // microservicio planes (Estefanía)
}
