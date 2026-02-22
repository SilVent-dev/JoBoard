package br.com.joboard.dominio.entidade;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "perfil_candidato", indexes = {
        @Index(name = "idx_perfilcandidato", columnList = "usuario_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_perfilcandidato_usuario_id", columnNames = {"usuario_id"})
})
public class PerfilCandidato {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @CPF //alterar validação para um DTO?
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(name = "aceita_remoto", nullable = false)
    private Boolean aceitaRemoto = true;

    @Column(name = "aceita_hibrido", nullable = false)
    private Boolean aceitaHibrido = true;

    @Column(name = "aceita_presencial", nullable = false)
    private Boolean aceitaPresencial = true;

    @Column(name = "nivel_experiencia", length = 20)
    private String nivelExperiencia;

    @Column(length = 50)
    private String disponibilidade;

    @Column(name = "pretensao_salarial_min", precision = 10, scale = 2)
    private BigDecimal pretensaoSalarialMin;

    @Column(name = "pretensao_salarial_max", precision = 10, scale = 2)
    private BigDecimal pretensaoSalarialMax;

    @Column(name = "url_linkedin", length = 1000)
    private String urlLinkedin;

    @Column(name = "url_github", length = 1000)
    private String urlGithub;

    @Column(name = "url_portfolio", length = 1000)
    private String urlPortfolio;

    @Column(name = "resumo_profissional", columnDefinition = "TEXT")
    private String resumoProfissional;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PerfilCandidato that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(usuario, that.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuario);
    }
}
