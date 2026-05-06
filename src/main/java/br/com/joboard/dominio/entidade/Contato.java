package br.com.joboard.dominio.entidade;

import br.com.joboard.dominio.enums.TipoContatoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contatos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidatura_id", nullable = false)
    private Candidatura candidatura;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(length = 255)
    private String cargo;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String telefone;

    @Column(length = 500)
    private String linkedin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoContatoEnum tipoContato;

    @Column(columnDefinition = "TEXT")
    private String interacaoPrincipal;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void preencherCriadoEm() {
        this.criadoEm = LocalDateTime.now();
    }
}