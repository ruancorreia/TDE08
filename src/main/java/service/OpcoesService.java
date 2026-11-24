package service;

import dto.OpcaoResponse;
import repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OpcoesService {
    private final PlanoRepository planos;
    private final RegistroPessoaRepository registros;
    private final UsuarioRepository usuarios;
    private final ConsultaRepository consultas;
    private final AplicacaoDoseRepository aplicacoes;

    public OpcoesService(PlanoRepository planos, RegistroPessoaRepository registros,
            UsuarioRepository usuarios, ConsultaRepository consultas, AplicacaoDoseRepository aplicacoes) {
        this.planos = planos;
        this.registros = registros;
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.aplicacoes = aplicacoes;
    }

    public List<OpcaoResponse> planos() { return planos.findAll().stream().map(p -> new OpcaoResponse(p.getId(), p.getNome())).toList(); }
    public List<OpcaoResponse> registros() { return registros.findAll().stream().map(r -> new OpcaoResponse(r.getId(), r.getNome())).toList(); }
    public List<OpcaoResponse> usuarios() { return usuarios.findAll().stream().map(u -> new OpcaoResponse(u.getId(), u.getNome())).toList(); }
    public List<OpcaoResponse> consultas() { return consultas.findAll().stream().map(c -> new OpcaoResponse(c.getId(), c.getDescricao())).toList(); }
    public List<OpcaoResponse> aplicacoes() { return aplicacoes.findAll().stream().map(a -> new OpcaoResponse(a.getId(), a.getDescricao())).toList(); }
}
