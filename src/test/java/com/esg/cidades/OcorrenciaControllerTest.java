package com.esg.cidades;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OcorrenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALIDO = """
            {"titulo":"Descarte irregular de lixo","descricao":"Entulho na calcada",
             "categoria":"AMBIENTAL","bairro":"Centro"}
            """;

    @Test
    void criaOcorrenciaEDevolveStatusAberta() throws Exception {
        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void listaOcorrenciasPorCategoria() throws Exception {
        mockMvc.perform(post("/api/ocorrencias")
                .contentType(MediaType.APPLICATION_JSON).content(VALIDO));

        mockMvc.perform(get("/api/ocorrencias").param("categoria", "AMBIENTAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("AMBIENTAL"));
    }

    @Test
    void rejeitaOcorrenciaSemTitulo() throws Exception {
        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"SOCIAL\",\"bairro\":\"Centro\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retorna404ParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/ocorrencias/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void infoMostraAmbiente() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiente").value("test"));
    }

    @Test
    void healthEstaUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
