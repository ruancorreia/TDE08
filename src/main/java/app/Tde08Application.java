package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"app", "controller", "service"})
@EntityScan("model")
@EnableJpaRepositories("repository")
public class Tde08Application {
    public static void main(String[] args) {
        SpringApplication.run(Tde08Application.class, args);
    }
}
