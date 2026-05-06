package br.com.joboard.dominio.entidade;

import br.com.joboard.dominio.enums.PlataformaAplicacaoEnum;
import br.com.joboard.dominio.enums.ResultadoFinalEnum;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "candidaturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidatura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_rastreada_id", nullable = false)
    private VagaRastreada vaga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculo_id")
    private Curriculo curriculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatusProcessoSeletivoEnum status = StatusProcessoSeletivoEnum.LISTA_DESEJO;

    private LocalDate dataAplicacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PlataformaAplicacaoEnum plataformaAplicacao;

    @Column(columnDefinition = "TEXT")
    private String cartaApresentacao;

    @Column(length = 500)
    private String portfolioEnviado;

    private LocalDate proximaAcaoEm;

    @Column(length = 255)
    private String proximaAcaoDescricao;

    @Column(precision = 2, scale = 1)
    private BigDecimal minhaAvaliacaoInteresse;

    @Column(precision = 2, scale = 1)
    private BigDecimal minhaAvaliacaoFit;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ResultadoFinalEnum resultadoFinal;

    @Column(columnDefinition = "TEXT")
    private String feedbackRecebido;

    @Column(length = 255)
    private String motivoRejeicao;

    @Builder.Default
    @Column(nullable = false)
    private boolean arquivada = false;

    private LocalDateTime arquivadaEm;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}