package repository;

import model.RegistroPessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroPessoaRepository extends JpaRepository<RegistroPessoa, Long> {
}
