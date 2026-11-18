package cl.duoc.predios.controller;

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

// Prueba los 10 endpoints como si fuera Postman, pero automático (MockMvc).
@SpringBootTest
@AutoConfigureMockMvc
class PredioSectorControllerTest {

    @Autowired
    private MockMvc mvc;

    // Crea un predio por POST y devuelve el id que le puso el servidor
    private long crearPredio(String nombre) throws Exception {
        String json = "{\"usuarioId\":7,\"nombre\":\"" + nombre + "\",\"comuna\":\"Quillota\",\"superficieHa\":10}";
        String respuesta = mvc.perform(post("/api/predios").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void crudCompletoDePredio() throws Exception {
        long id = crearPredio("Los Aromos");

        mvc.perform(get("/api/predios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Los Aromos"));

        mvc.perform(get("/api/predios?usuarioId=7")).andExpect(status().isOk());
        mvc.perform(get("/api/predios")).andExpect(status().isOk());

        mvc.perform(put("/api/predios/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":7,\"nombre\":\"Las Palmas\",\"comuna\":\"Quillota\",\"superficieHa\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Las Palmas"));

        mvc.perform(delete("/api/predios/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/predios/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void erroresDePredio() throws Exception {
        // 400: falta el nombre
        mvc.perform(post("/api/predios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":7,\"comuna\":\"Quillota\",\"superficieHa\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el nombre"));

        // 400 al actualizar con datos malos
        mvc.perform(put("/api/predios/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        // 404: no existe
        mvc.perform(put("/api/predios/9999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":7,\"nombre\":\"X\",\"comuna\":\"Y\",\"superficieHa\":1}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/predios/9999")).andExpect(status().isNotFound());
    }

    @Test
    void crudCompletoDeSector() throws Exception {
        long predioId = crearPredio("Con sectores");
        String json = "{\"predioId\":" + predioId + ",\"nombre\":\"Norte\",\"cultivo\":\"palto\",\"superficieHa\":4}";

        String respuesta = mvc.perform(post("/api/sectores").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(respuesta.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(get("/api/sectores?predioId=" + predioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Norte"));
        mvc.perform(get("/api/sectores")).andExpect(status().isOk());
        mvc.perform(get("/api/sectores/" + id)).andExpect(status().isOk());

        mvc.perform(put("/api/sectores/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("Norte", "Sur")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sur"));

        mvc.perform(delete("/api/sectores/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/sectores/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void erroresDeSector() throws Exception {
        // 400: el predio no existe
        mvc.perform(post("/api/sectores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"predioId\":9999,\"nombre\":\"N\",\"cultivo\":\"palto\",\"superficieHa\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El predio no existe"));

        mvc.perform(put("/api/sectores/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        long predioId = crearPredio("Para 404");
        mvc.perform(put("/api/sectores/9999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"predioId\":" + predioId + ",\"nombre\":\"N\",\"cultivo\":\"palto\",\"superficieHa\":1}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/sectores/9999")).andExpect(status().isNotFound());
    }
}
