package controller;

import dto.OpcaoResponse;
import service.OpcoesService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OpcoesController {
    private final OpcoesService service;
    public OpcoesController(OpcoesService service) { this.service = service; }

    @GetMapping("/planos") public List<OpcaoResponse> planos() { return service.planos(); }
    @GetMapping("/registros-pessoa") public List<OpcaoResponse> registros() { return service.registros(); }
    @GetMapping("/usuarios") public List<OpcaoResponse> usuarios() { return service.usuarios(); }
    @GetMapping("/consultas") public List<OpcaoResponse> consultas() { return service.consultas(); }
    @GetMapping("/aplicacoes-dose") public List<OpcaoResponse> aplicacoes() { return service.aplicacoes(); }
}
