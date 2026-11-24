package dto;

import model.Paciente;
import model.Consulta;
import model.AplicacaoDose;
import java.util.List;

public record PacienteResponse(Long id, String tipoSanguineo, String numeroSus,
    Long planoId, Long registroPessoaId, Long usuarioId,
    List<Long> consultaIds, List<Long> aplicacaoDoseIds) {

    public static PacienteResponse de(Paciente paciente) {
        return new PacienteResponse(paciente.getId(), paciente.getTipoSanguineo().getRotulo(),
            paciente.getNumeroSus(), paciente.getPlano().getId(), paciente.getRegistroPessoa().getId(),
            paciente.getUsuario().getId(),
            paciente.getListaConsultas().stream().map(Consulta::getId).toList(),
            paciente.getListaAplicacoes().stream().map(AplicacaoDose::getId).toList());
    }
}
