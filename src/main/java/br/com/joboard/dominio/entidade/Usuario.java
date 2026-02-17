package br.com.joboard.dominio.entidade;

import br.com.joboard.dominio.enums.StatusContaEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email obrigatório")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Senha obrigatória")
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @NotBlank(message = "Nome obrigatório")
    @Column(nullable = false, length = 255)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_conta", nullable = false, length = 40)
    private StatusContaEnum statusConta = StatusContaEnum.CADASTRO_PENDENTE_CONFIRMACAO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "ultimo_login_em")
    private LocalDateTime ultimoLoginEm;

    @Column(name = "desativado_em")
    private LocalDateTime desativadoEm;

    @Column(name = "bloqueado_em")
    private LocalDateTime bloqueadoEm;

    @Column(name = "motivo_bloqueio", columnDefinition = "TEXT")
    private String motivoBloqueio;

    @Column(name = "excluido_em")
    private LocalDateTime excluidoEm;

    public boolean podeAutenticar() { // Metodo de negócio que valida se conta está ATIVA. Spring Security usará isso para bloquear login de contas pendentes/bloqueadas.
        return statusConta == StatusContaEnum.ATIVO;
    }

    public void registrarLogin() { // Atualiza campo ultimoLoginEm quando autenticação for bem-sucedida
        this.ultimoLoginEm = LocalDateTime.now();
    }

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "usuarios_tipo_usuario",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_usuario_id"))
    private List<TipoUsuario> tipoUsuario;


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Usuario usuario = (Usuario) o;
        return getId() != null && Objects.equals(getId(), usuario.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
