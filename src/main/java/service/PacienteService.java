package service;

import dto.PacienteRequest;
import dto.PacienteResponse;
import model.*;
import repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PacienteService {
    private final PacienteRepository pacientes;
    private final PlanoRepository planos;
    private final RegistroPessoaRepository registros;
    private final UsuarioRepository usuarios;
    private final ConsultaRepository consultas;
    private final AplicacaoDoseRepository aplicacoes;

    public PacienteService(PacienteRepository pacientes, PlanoRepository planos,
            RegistroPessoaRepository registros, UsuarioRepository usuarios,
            ConsultaRepository consultas, AplicacaoDoseRepository aplicacoes) {
        this.pacientes = pacientes;
        this.planos = planos;
        this.registros = registros;
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.aplicacoes = aplicacoes;
    }

    public List<PacienteResponse> listar() {
        return pacientes.findAll().stream().map(PacienteResponse::de).toList();
    }

    public PacienteResponse buscar(Long id) {
        return PacienteResponse.de(buscarEntidade(id));
    }

    @Transactional(rollbackFor = ModelException.class)
    public PacienteResponse cadastrar(PacienteRequest dados) throws ModelException {
        return salvar(new Paciente(), dados);
    }

    @Transactional(rollbackFor = ModelException.class)
    public PacienteResponse atualizar(Long id, PacienteRequest dados) throws ModelException {
        return salvar(buscarEntidade(id), dados);
    }

    @Transactional
    public void excluir(Long id) {
        Paciente paciente = buscarEntidade(id);
        // Os registros relacionados já existem: desvincular, sem apagá-los.
        desvincularConsultas(paciente, List.of());
        desvincularAplicacoes(paciente, List.of());
        paciente.getRegistroPessoa().setPaciente(null);
        paciente.getUsuario().removerPaciente(paciente);
        pacientes.delete(paciente);
        pacientes.flush();
    }

    private Paciente buscarEntidade(Long id) {
        return pacientes.findById(id).orElseThrow(() -> inexistente("Paciente", id));
    }

    private PacienteResponse salvar(Paciente paciente, PacienteRequest dados) throws ModelException {
        Long id = paciente.getId();
        TipoSanguineo tipo = TipoSanguineo.deRotulo(dados.tipoSanguineo());
        Paciente.validarNumeroSus(dados.numeroSus());
        if (!dados.numeroSus().matches("[0-9]{14}")) {
            throw new ModelException("O número do SUS deve conter 14 dígitos.");
        }
        boolean duplicado = id == null ? pacientes.existsByNumeroSus(dados.numeroSus())
                : pacientes.existsByNumeroSusAndIdNot(dados.numeroSus(), id);
        if (duplicado) throw conflito("Número do SUS já cadastrado.");

        Plano plano = planos.findById(dados.planoId())
                .orElseThrow(() -> inexistente("Plano", dados.planoId()));
        RegistroPessoa registro = registros.findById(dados.registroPessoaId())
                .orElseThrow(() -> inexistente("Registro de pessoa", dados.registroPessoaId()));
        Usuario usuario = usuarios.findById(dados.usuarioId())
                .orElseThrow(() -> inexistente("Usuário", dados.usuarioId()));
        pacientes.findByPlano_Id(plano.getId()).ifPresent(p -> verificarVinculo(p, id, "Plano"));
        pacientes.findByRegistroPessoa_Id(registro.getId()).ifPresent(p -> verificarVinculo(p, id, "Registro de pessoa"));

        List<Consulta> selecionadas = new ArrayList<>();
        for (Long consultaId : ids(dados.consultaIds())) {
            Consulta consulta = consultas.findById(consultaId)
                    .orElseThrow(() -> inexistente("Consulta", consultaId));
            verificarVinculo(consulta.getPaciente(), id, "Consulta");
            selecionadas.add(consulta);
        }
        List<AplicacaoDose> doses = new ArrayList<>();
        for (Long doseId : ids(dados.aplicacaoDoseIds())) {
            AplicacaoDose dose = aplicacoes.findById(doseId)
                    .orElseThrow(() -> inexistente("Aplicação de dose", doseId));
            verificarVinculo(dose.getPaciente(), id, "Aplicação de dose");
            doses.add(dose);
        }

        paciente.setTipoSanguineo(tipo);
        paciente.setNumeroSus(dados.numeroSus());
        paciente.setPlano(plano);
        if (paciente.getRegistroPessoa() != null && !Objects.equals(paciente.getRegistroPessoa().getId(), registro.getId())) {
            paciente.getRegistroPessoa().setPaciente(null);
        }
        paciente.setRegistroPessoa(registro);
        if (paciente.getUsuario() != null && !Objects.equals(paciente.getUsuario().getId(), usuario.getId())) {
            paciente.getUsuario().removerPaciente(paciente);
        }
        paciente.setUsuario(usuario);
        paciente = pacientes.save(paciente);

        desvincularConsultas(paciente, selecionadas);
        for (Consulta consulta : selecionadas) paciente.adicionarConsulta(consulta);
        desvincularAplicacoes(paciente, doses);
        for (AplicacaoDose dose : doses) paciente.adicionarAplicacao(dose);
        // As entidades relacionadas estão gerenciadas pela transação JPA.
        pacientes.flush();
        return PacienteResponse.de(paciente);
    }

    private void desvincularConsultas(Paciente paciente, List<Consulta> manter) {
        for (Consulta consulta : paciente.getListaConsultas()) {
            if (!manter.contains(consulta)) {
                paciente.removerConsulta(consulta);
                consulta.setPaciente(null);
            }
        }
    }

    private void desvincularAplicacoes(Paciente paciente, List<AplicacaoDose> manter) {
        for (AplicacaoDose dose : paciente.getListaAplicacoes()) {
            if (!manter.contains(dose)) {
                paciente.removerAplicacao(dose);
                dose.setPaciente(null);
            }
        }
    }

    private Set<Long> ids(List<Long> valores) {
        return valores == null ? Set.of() : new LinkedHashSet<>(valores);
    }

    private void verificarVinculo(Paciente outro, Long id, String entidade) {
        if (outro != null && !Objects.equals(outro.getId(), id)) {
            throw conflito(entidade + " já vinculada a outro paciente.");
        }
    }

    private ResponseStatusException inexistente(String entidade, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, entidade + " não encontrado(a): " + id);
    }

    private ResponseStatusException conflito(String mensagem) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensagem);
    }
}
