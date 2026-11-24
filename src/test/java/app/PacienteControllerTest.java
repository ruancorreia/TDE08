package app;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.PacienteRequest;
import repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:controller_test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class PacienteControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PlanoRepository planos;
    @Autowired RegistroPessoaRepository registros;
    @Autowired UsuarioRepository usuarios;
    @Autowired ConsultaRepository consultas;
    @Autowired AplicacaoDoseRepository aplicacoes;

    private PacienteRequest dados(String sus, int indice) {
        return new PacienteRequest("AB+", sus, planos.findAll().get(indice).getId(),
            registros.findAll().get(indice).getId(), usuarios.findAll().get(indice).getId(),
            List.of(consultas.findAll().get(indice).getId()), List.of(aplicacoes.findAll().get(indice).getId()));
    }

    @Test
    void deveExecutarCrudPelosEndpoints() throws Exception {
        String resposta = mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dados("12345678901234", 0))))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.tipoSanguineo").value("AB+"))
            .andExpect(jsonPath("$.consultaIds[0]").isNumber())
            .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(resposta).get("id").asLong();
        mvc.perform(get("/api/pacientes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/pacientes/{id}", id)).andExpect(status().isOk());
        mvc.perform(put("/api/pacientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dados("98765432109876", 1))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.numeroSus").value("98765432109876"));
        mvc.perform(delete("/api/pacientes/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/pacientes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ParaSusInvalido() throws Exception {
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dados("123", 0))))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveRetornar409ParaSusDuplicado() throws Exception {
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dados("12345678901234", 0))))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dados("12345678901234", 1))))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveRetornar404ParaPacienteInexistente() throws Exception {
        mvc.perform(get("/api/pacientes/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void deveDisponibilizarOpcoesUtilizadasNoTde07() throws Exception {
        for (String caminho : List.of("planos", "registros-pessoa", "usuarios", "consultas", "aplicacoes-dose")) {
            mvc.perform(get("/api/" + caminho)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber()).andExpect(jsonPath("$[0].nome").isString());
        }
    }
}
