package br.com.joboard.dominio.entidade;

import br.com.joboard.dominio.enums.ModeloTrabalhoEnum;
import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoContratoEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vagas_rastreadas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VagaRastreada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_catalogada_id", nullable = false)
    private EmpresaCatalogada empresa;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 500)
    private String urlVaga;

    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ModeloTrabalhoEnum modeloTrabalho;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TipoContratoEnum tipoContrato;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NivelExperienciaEnum nivelExperiencia;

    @Column(length = 100)
    private String faixaSalarial;

    @Column(columnDefinition = "TEXT")
    private String beneficios;

    @Column(columnDefinition = "TEXT")
    private String requisitosObrigatorios;

    @Column(columnDefinition = "TEXT")
    private String requisitosDesejaveis;

    @Builder.Default
    @Column(nullable = false)
    private boolean vagaAindaAberta = true;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}