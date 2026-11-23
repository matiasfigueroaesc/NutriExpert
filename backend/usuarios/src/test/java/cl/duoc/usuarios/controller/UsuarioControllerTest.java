package cl.duoc.usuarios.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba los 5 endpoints como si fuera Postman, pero automático (MockMvc) y con H2.
// Cada test usa un correo distinto porque la base H2 se comparte entre tests.
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    // Registra un usuario por POST y devuelve el id que le puso el servidor
    private long registrar(String email) throws Exception {
        String json = "{\"nombre\":\"Fabián\",\"email\":\"" + email + "\",\"password\":\"agricola26\"}";
        String respuesta = mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist()) // la contraseña nunca se devuelve
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private String login(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    // El test que pide la guía: registrar un usuario y después hacer login
    @Test
    void registrarYDespuesHacerLogin() throws Exception {
        long id = registrar("fabian@nutriexpert.cl");

        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("fabian@nutriexpert.cl", "agricola26")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Fabián"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginNoDistingueMayusculasNiEspaciosEnElCorreo() throws Exception {
        registrar("mati@nutriexpert.cl");

        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("  Mati@NutriExpert.cl ", "agricola26")))
                .andExpect(status().isOk());
    }

    @Test
    void erroresDeRegistroYLogin() throws Exception {
        registrar("estefania@nutriexpert.cl");

        // 400: el correo ya existe
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Otra\",\"email\":\"estefania@nutriexpert.cl\",\"password\":\"agricola26\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una cuenta con ese correo"));

        // 400: contraseña sin números
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Otra\",\"email\":\"otra@nutriexpert.cl\",\"password\":\"abcdefgh\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La contraseña debe tener letras y números"));

        // 401: contraseña incorrecta
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("estefania@nutriexpert.cl", "otraClave99")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));

        // 401: el correo no existe (mismo mensaje, para no revelar qué correos están registrados)
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("nadie@nutriexpert.cl", "agricola26")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));

        // 400: faltan datos en el login
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verEditarYBorrarPerfil() throws Exception {
        long id = registrar("perfil@nutriexpert.cl");

        mvc.perform(get("/api/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("perfil@nutriexpert.cl"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(put("/api/usuarios/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Fabián Rubio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Fabián Rubio"));

        // 400: nombre vacío
        mvc.perform(put("/api/usuarios/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        mvc.perform(delete("/api/usuarios/" + id)).andExpect(status().isNoContent());

        // 404: ya no existe
        mvc.perform(get("/api/usuarios/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/usuarios/" + id)).andExpect(status().isNotFound());
        mvc.perform(put("/api/usuarios/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X Y\"}"))
                .andExpect(status().isNotFound());
    }
}
