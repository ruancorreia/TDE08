package controller;

import model.ModelException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ModelException.class)
    public ResponseEntity<Map<String, String>> modelo(ModelException ex) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException ex) {
        var erro = ex.getBindingResult().getFieldError();
        return resposta(HttpStatus.BAD_REQUEST, erro == null ? "Dados inválidos." : erro.getField() + ": " + erro.getDefaultMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido() {
        return resposta(HttpStatus.BAD_REQUEST, "JSON inválido.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException ex) {
        return resposta(ex.getStatusCode(), ex.getReason() == null ? "Não foi possível concluir a operação." : ex.getReason());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridade() {
        return resposta(HttpStatus.CONFLICT, "A operação viola uma restrição do banco de dados.");
    }

    private ResponseEntity<Map<String, String>> resposta(HttpStatusCode status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("message", mensagem));
    }
}
