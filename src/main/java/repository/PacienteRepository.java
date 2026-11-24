package repository;

import model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    boolean existsByNumeroSus(String numeroSus);
    boolean existsByNumeroSusAndIdNot(String numeroSus, Long id);
    Optional<Paciente> findByPlano_Id(Long planoId);
    Optional<Paciente> findByRegistroPessoa_Id(Long registroPessoaId);
}
