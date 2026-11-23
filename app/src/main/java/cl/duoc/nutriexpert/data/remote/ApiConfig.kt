package cl.duoc.nutriexpert.data.remote

// Direcciones de los microservicios, en un solo lugar.
// SERVIDOR es la Elastic IP de la EC2 de AWS, donde corren los 3 microservicios.
// Para probar con los microservicios corriendo en tu PC (emulador), usa "http://10.0.2.2",
// que es "el PC" visto desde el emulador.
object ApiConfig {
    private const val SERVIDOR = "http://3.226.195.117"

    const val URL_USUARIOS = "$SERVIDOR:8080/"   // microservicio usuarios (Fabián)
    const val URL_PREDIOS = "$SERVIDOR:8081/"    // microservicio predios (predios y sectores)
    const val URL_PLANES = "$SERVIDOR:8082/"     // microservicio planes (Estefanía)
}
