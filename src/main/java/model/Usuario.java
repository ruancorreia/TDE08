package model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// Modelo auxiliar demonstrativo; a entidade original não foi fornecida.
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;

    public Usuario() {}
    public Usuario(String nome) { this.nome = nome; }
    public Long getId() { return id; }
    public String getNome() { return nome; }

    @OneToMany(mappedBy = "usuario")
    private List<Paciente> pacientes = new ArrayList<>();
    public void adicionarPaciente(Paciente paciente) {
        if (!pacientes.contains(paciente)) pacientes.add(paciente);
    }
    public void removerPaciente(Paciente paciente) { pacientes.remove(paciente); }
}
