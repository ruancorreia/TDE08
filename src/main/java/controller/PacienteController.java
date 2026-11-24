package controller;

import dto.PacienteRequest;
import dto.PacienteResponse;
import jakarta.validation.Valid;
import model.ModelException;
import service.PacienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {
    private final PacienteService service;

    public PacienteController(PacienteService service) { this.service = service; }

    @GetMapping
    public List<PacienteResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public PacienteResponse buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@Valid @RequestBody PacienteRequest dados) throws ModelException {
        PacienteResponse paciente = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/pacientes/" + paciente.id())).body(paciente);
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest dados) throws ModelException {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
