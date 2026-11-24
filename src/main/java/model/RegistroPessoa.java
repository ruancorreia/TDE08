package model;

import jakarta.persistence.*;

// Modelo auxiliar demonstrativo; a entidade original não foi fornecida.
@Entity
@Table(name = "registro_pessoa")
public class RegistroPessoa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;

    public RegistroPessoa() {}
    public RegistroPessoa(String nome) { this.nome = nome; }
    public Long getId() { return id; }
    public String getNome() { return nome; }

    @OneToOne(mappedBy = "registroPessoa", fetch = FetchType.LAZY)
    private Paciente paciente;
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
}
