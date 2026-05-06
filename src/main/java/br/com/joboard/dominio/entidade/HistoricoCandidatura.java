package br.com.joboard.dominio.entidade;

import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import br.com.joboard.dominio.enums.TipoEventoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_candidatura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoCandidatura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidatura_id", nullable = false)
    private Candidatura candidatura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoEventoEnum tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatusProcessoSeletivoEnum statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatusProcessoSeletivoEnum statusNovo;

    @Column(nullable = false, length = 255)
    private String tituloEvento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataEvento;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    @PrePersist
    void preencherRegistradoEm() {
        this.registradoEm = LocalDateTime.now();
    }
}