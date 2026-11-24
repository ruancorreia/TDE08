package model;

import jakarta.persistence.*;

// Modelo auxiliar demonstrativo; a entidade original não foi fornecida.
@Entity
@Table(name = "plano")
public class Plano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;

    public Plano() {}
    public Plano(String nome) { this.nome = nome; }
    public Long getId() { return id; }
    public String getNome() { return nome; }

}
