package app;

import dto.PacienteRequest;
import dto.PacienteResponse;
import model.*;
import repository.*;
import service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityManager;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:service_test;DB_CLOSE_DELAY=-1")
@Transactional
class PacienteServiceTest {
    @Autowired PacienteService service;
    @Autowired PacienteRepository pacientes;
    @Autowired PlanoRepository planos;
    @Autowired RegistroPessoaRepository registros;
    @Autowired UsuarioRepository usuarios;
    @Autowired ConsultaRepository consultas;
    @Autowired AplicacaoDoseRepository aplicacoes;
    @Autowired EntityManager em;

    private PacienteRequest dados(int indice, String sus, List<Long> consultaIds, List<Long> doses) {
        return new PacienteRequest("A+", sus, planos.findAll().get(indice).getId(),
            registros.findAll().get(indice).getId(), usuarios.findAll().get(indice).getId(), consultaIds, doses);
    }

    @Test
    void deveCadastrarConsultarAtualizarExcluirPreservandoRelacionamentos() throws ModelException {
        Long consulta = consultas.findAll().get(0).getId();
        Long dose = aplicacoes.findAll().get(0).getId();
        PacienteRequest dados = dados(0, "12345678901234", List.of(consulta), List.of(dose));
        PacienteResponse criado = service.cadastrar(dados);
        em.clear();
        assertThat(service.buscar(criado.id()).consultaIds()).containsExactly(consulta);
        assertThat(service.buscar(criado.id()).aplicacaoDoseIds()).containsExactly(dose);
        assertThat(service.listar()).extracting(PacienteResponse::id).contains(criado.id());
        PacienteRequest alteracao = dados(1, "98765432109876", List.of(), List.of(dose));
        service.atualizar(criado.id(), alteracao);
        em.clear();
        PacienteResponse atualizado = service.buscar(criado.id());
        assertThat(atualizado.numeroSus()).isEqualTo("98765432109876");
        assertThat(atualizado.registroPessoaId()).isEqualTo(alteracao.registroPessoaId());
        assertThat(atualizado.usuarioId()).isEqualTo(alteracao.usuarioId());
        assertThat(atualizado.consultaIds()).isEmpty();
        assertThat(consultas.findById(consulta).orElseThrow().getPaciente()).isNull();
        service.excluir(criado.id());
        em.clear();
        assertThat(pacientes.findById(criado.id())).isEmpty();
        assertThat(planos.existsById(alteracao.planoId())).isTrue();
        assertThat(registros.existsById(alteracao.registroPessoaId())).isTrue();
        assertThat(aplicacoes.findById(dose).orElseThrow().getPaciente()).isNull();
    }

    @Test
    void deveRejeitarSusDuplicado() throws ModelException {
        service.cadastrar(dados(0, "12345678901234", List.of(), List.of()));
        var repetido = dados(1, "12345678901234", List.of(), List.of());
        assertThatThrownBy(() -> service.cadastrar(repetido))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
    }

    @Test
    void deveRejeitarConsultaDeOutroPaciente() throws ModelException {
        Long consulta = consultas.findAll().get(0).getId();
        service.cadastrar(dados(0, "12345678901234", List.of(consulta), List.of()));
        var outro = dados(1, "98765432109876", List.of(consulta), List.of());
        assertThatThrownBy(() -> service.cadastrar(outro))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409");
    }

    @Test
    void deveRejeitarTipoSanguineoInvalido() {
        var d = dados(0, "12345678901234", List.of(), List.of());
        var invalido = new PacienteRequest("X+", d.numeroSus(), d.planoId(), d.registroPessoaId(), d.usuarioId(), List.of(), List.of());
        assertThatThrownBy(() -> service.cadastrar(invalido)).isInstanceOf(ModelException.class);
    }

    @Test
    void deveInformarPacienteInexistente() {
        assertThatThrownBy(() -> service.buscar(Long.MAX_VALUE))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
    }
}
