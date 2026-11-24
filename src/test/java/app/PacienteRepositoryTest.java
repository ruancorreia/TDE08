package app;

import model.*;
import repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "app.demo.enabled=false")
class PacienteRepositoryTest {
    @Autowired PacienteRepository pacientes;
    @Autowired PlanoRepository planos;
    @Autowired RegistroPessoaRepository registros;
    @Autowired UsuarioRepository usuarios;
    @Autowired EntityManager em;

    private Paciente novo(String sus) throws ModelException {
        return new Paciente(TipoSanguineo.AP, sus, planos.save(new Plano("Plano teste")),
            registros.save(new RegistroPessoa("Pessoa teste")), usuarios.save(new Usuario("Usuário teste")));
    }

    @Test
    void devePersistirConsultarAtualizarExcluir() throws ModelException {
        Paciente paciente = pacientes.saveAndFlush(novo("12345678901234"));
        Long id = paciente.getId();
        em.clear();
        assertThat(pacientes.existsByNumeroSus("12345678901234")).isTrue();
        Paciente encontrado = pacientes.findById(id).orElseThrow();
        assertThat(encontrado.getTipoSanguineo()).isEqualTo(TipoSanguineo.AP);
        assertThat(pacientes.findAll()).extracting(Paciente::getId).contains(id);
        encontrado.setTipoSanguineo(TipoSanguineo.OM);
        pacientes.saveAndFlush(encontrado);
        em.clear();
        assertThat(pacientes.findById(id).orElseThrow().getTipoSanguineo()).isEqualTo(TipoSanguineo.OM);
        pacientes.deleteById(id);
        pacientes.flush();
        em.clear();
        assertThat(pacientes.findById(id)).isEmpty();
    }

    @Test
    void deveImpedirNumeroSusDuplicadoNoBanco() throws ModelException {
        pacientes.saveAndFlush(novo("12345678901234"));
        Paciente duplicado = novo("12345678901234");
        assertThatThrownBy(() -> pacientes.saveAndFlush(duplicado))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
