package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.CadastroDTO;
import br.com.joboard.dominio.entidade.TokenRedefinicaoSenha;
import br.com.joboard.dominio.entidade.TokenVerificacaoEmail;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusContaEnum;
import br.com.joboard.dominio.evento.SolicitacaoRedefinicaoSenhaEvento;
import br.com.joboard.dominio.excecao.TokenRedefinicaoInvalidoException;
import br.com.joboard.dominio.excecao.TokenVerificacaoInvalidoException;
import br.com.joboard.repositorio.TipoUsuarioRepositorio;
import br.com.joboard.repositorio.TokenRedefinicaoSenhaRepositorio;
import br.com.joboard.repositorio.TokenVerificacaoEmailRepositorio;
import br.com.joboard.repositorio.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tokens de uso único (verificação de email e redefinição de senha):
 * inexistente, já usado ou expirado deve falhar com exceção de domínio,
 * e o uso legítimo deve invalidar o token. O fluxo de esqueci-senha
 * nunca revela se o email existe.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServicoTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private TipoUsuarioRepositorio tipoUsuarioRepositorio;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenVerificacaoEmailRepositorio tokenRepositorio;
    @Mock
    private EmailServico emailServico;
    @Mock
    private TokenRedefinicaoSenhaRepositorio tokenRedefinicaoRepositorio;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UsuarioServico usuarioServico;

    @BeforeEach
    void setUp() {
        usuarioServico = new UsuarioServico(usuarioRepositorio, tipoUsuarioRepositorio,
                passwordEncoder, tokenRepositorio, emailServico,
                tokenRedefinicaoRepositorio, eventPublisher);
    }

    // ── Verificação de email ─────────────────────────────────────────

    private TokenVerificacaoEmail tokenVerificacaoValido() {
        return TokenVerificacaoEmail.builder()
                .token("token-abc")
                .usuario(Usuario.builder()
                        .statusConta(StatusContaEnum.CADASTRO_PENDENTE_CONFIRMACAO)
                        .build())
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(2))
                .build();
    }

    @Test
    void verificarEmail_tokenInexistente_deveLancarTokenInvalido() {
        when(tokenRepositorio.findByToken("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioServico.verificarEmail("nao-existe"))
                .isInstanceOf(TokenVerificacaoInvalidoException.class);
    }

    @Test
    void verificarEmail_tokenJaUsado_deveLancarTokenInvalido() {
        TokenVerificacaoEmail token = tokenVerificacaoValido();
        token.setUsadoEm(LocalDateTime.now().minusMinutes(5));
        when(tokenRepositorio.findByToken("token-abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> usuarioServico.verificarEmail("token-abc"))
                .isInstanceOf(TokenVerificacaoInvalidoException.class);
    }

    @Test
    void verificarEmail_tokenExpirado_deveLancarTokenInvalido() {
        TokenVerificacaoEmail token = tokenVerificacaoValido();
        token.setExpiraEm(LocalDateTime.now().minusMinutes(1));
        when(tokenRepositorio.findByToken("token-abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> usuarioServico.verificarEmail("token-abc"))
                .isInstanceOf(TokenVerificacaoInvalidoException.class);
    }

    @Test
    void verificarEmail_tokenValido_deveAtivarContaEInvalidarToken() {
        TokenVerificacaoEmail token = tokenVerificacaoValido();
        when(tokenRepositorio.findByToken("token-abc")).thenReturn(Optional.of(token));

        usuarioServico.verificarEmail("token-abc");

        assertThat(token.getUsuario().getStatusConta()).isEqualTo(StatusContaEnum.ATIVO);
        assertThat(token.foiUsado()).isTrue();
    }

    // ── Esqueci minha senha ──────────────────────────────────────────

    private final Usuario usuarioAtivo = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario@teste.com")
            .nome("Usuária")
            .statusConta(StatusContaEnum.ATIVO)
            .build();

    @Test
    void solicitarRedefinicao_emailInexistente_naoPublicaEventoNemFalha() {
        when(usuarioRepositorio.findByEmail("nao-existe@teste.com")).thenReturn(Optional.empty());

        usuarioServico.solicitarRedefinicaoSenha("nao-existe@teste.com");

        verify(eventPublisher, never()).publishEvent(any());
        verify(tokenRedefinicaoRepositorio, never()).save(any());
    }

    @Test
    void solicitarRedefinicao_emailExistente_geraTokenEPublicaEvento() {
        when(usuarioRepositorio.findByEmail(usuarioAtivo.getEmail()))
                .thenReturn(Optional.of(usuarioAtivo));
        when(tokenRedefinicaoRepositorio.countByUsuarioIdAndCriadoEmAfter(
                any(), any())).thenReturn(0L);
        when(tokenRedefinicaoRepositorio.encontrarPendentePorUsuario(usuarioAtivo.getId()))
                .thenReturn(Optional.empty());

        usuarioServico.solicitarRedefinicaoSenha(usuarioAtivo.getEmail());

        verify(tokenRedefinicaoRepositorio).save(any(TokenRedefinicaoSenha.class));
        verify(eventPublisher).publishEvent(any(SolicitacaoRedefinicaoSenhaEvento.class));
    }

    @Test
    void solicitarRedefinicao_limitePorEmailAtingido_naoGeraTokenNemEvento() {
        when(usuarioRepositorio.findByEmail(usuarioAtivo.getEmail()))
                .thenReturn(Optional.of(usuarioAtivo));
        when(tokenRedefinicaoRepositorio.countByUsuarioIdAndCriadoEmAfter(
                any(), any())).thenReturn(3L);

        usuarioServico.solicitarRedefinicaoSenha(usuarioAtivo.getEmail());

        verify(tokenRedefinicaoRepositorio, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private TokenRedefinicaoSenha tokenRedefinicaoValido() {
        return TokenRedefinicaoSenha.builder()
                .token("reset-abc")
                .usuario(usuarioAtivo)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    @Test
    void redefinirSenha_tokenInexistente_deveLancarTokenInvalido() {
        when(tokenRedefinicaoRepositorio.findByToken("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioServico.redefinirSenha("nao-existe", "senhaNova123"))
                .isInstanceOf(TokenRedefinicaoInvalidoException.class);
    }

    @Test
    void redefinirSenha_tokenJaUsado_deveLancarTokenInvalido() {
        TokenRedefinicaoSenha token = tokenRedefinicaoValido();
        token.setUsadoEm(LocalDateTime.now().minusMinutes(1));
        when(tokenRedefinicaoRepositorio.findByToken("reset-abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> usuarioServico.redefinirSenha("reset-abc", "senhaNova123"))
                .isInstanceOf(TokenRedefinicaoInvalidoException.class);
    }

    @Test
    void redefinirSenha_tokenExpirado_deveLancarTokenInvalido() {
        TokenRedefinicaoSenha token = tokenRedefinicaoValido();
        token.setExpiraEm(LocalDateTime.now().minusMinutes(1));
        when(tokenRedefinicaoRepositorio.findByToken("reset-abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> usuarioServico.redefinirSenha("reset-abc", "senhaNova123"))
                .isInstanceOf(TokenRedefinicaoInvalidoException.class);
    }

    @Test
    void redefinirSenha_tokenValido_fazHashDaNovaSenhaEInvalidaToken() {
        TokenRedefinicaoSenha token = tokenRedefinicaoValido();
        when(tokenRedefinicaoRepositorio.findByToken("reset-abc")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("senhaNova123")).thenReturn("$2a$hash-novo");

        usuarioServico.redefinirSenha("reset-abc", "senhaNova123");

        assertThat(usuarioAtivo.getSenhaHash()).isEqualTo("$2a$hash-novo");
        assertThat(token.foiUsado()).isTrue();
        verify(usuarioRepositorio).save(usuarioAtivo);
    }

    // ── Honeypot no cadastro ─────────────────────────────────────────

    @Test
    void cadastrar_comHoneypotPreenchido_descartaSilenciosamente() {
        CadastroDTO bot = new CadastroDTO("Bot", "bot@teste.com", "12345678", "http://spam.com");

        usuarioServico.cadastrar(bot);

        verify(usuarioRepositorio, never()).save(any());
        verify(emailServico, never()).enviarVerificacao(any(), any(), any());
    }
}
