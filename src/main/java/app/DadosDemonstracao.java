package app;

import model.*;
import repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DadosDemonstracao {
    @Bean
    CommandLineRunner carregarDados(PlanoRepository planos, RegistroPessoaRepository registros,
            UsuarioRepository usuarios, ConsultaRepository consultas, AplicacaoDoseRepository aplicacoes) {
        return args -> {
            for (int i = 1; i <= 3; i++) {
                planos.save(new Plano("Plano demonstrativo " + i));
                registros.save(new RegistroPessoa("Pessoa fictícia " + i));
                usuarios.save(new Usuario("Usuário demonstrativo " + i));
                consultas.save(new Consulta("Consulta demonstrativa " + i));
                aplicacoes.save(new AplicacaoDose("Aplicação demonstrativa " + i));
            }
        };
    }
}
