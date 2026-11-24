package dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record PacienteRequest(
    @NotBlank String tipoSanguineo,
    @NotBlank @Pattern(regexp = "[0-9]{14}", message = "deve conter 14 dígitos") String numeroSus,
    @NotNull @Positive Long planoId,
    @NotNull @Positive Long registroPessoaId,
    @NotNull @Positive Long usuarioId,
    List<@NotNull @Positive Long> consultaIds,
    List<@NotNull @Positive Long> aplicacaoDoseIds
) {}
