package model;

import jakarta.persistence.*;

// Modelo auxiliar demonstrativo; a entidade original não foi fornecida.
@Entity
@Table(name = "consulta")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String descricao;

    public Consulta() {}
    public Consulta(String descricao) { this.descricao = descricao; }
    public Long getId() { return id; }
    public String getDescricao() { return descricao; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
}
