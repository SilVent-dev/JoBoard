package br.com.joboard.dominio.entidade;

import br.com.joboard.dominio.enums.PorteEmpresaEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "empresas_catalogadas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_usuario_empresa",
                        columnNames = {"usuario_id", "nome"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaCatalogada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nome;

    @Column(length = 500)
    private String site;

    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PorteEmpresaEnum porte;

    @Column(length = 100)
    private String setor;

    @Column(columnDefinition = "TEXT")
    private String culturaObservacoes;

    private String contatoRh;
    private String contatoReferencia;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}