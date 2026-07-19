package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.CadastroDTO;
import br.com.joboard.dominio.entidade.TipoUsuario;
import br.com.joboard.dominio.entidade.TokenRedefinicaoSenha;
import br.com.joboard.dominio.entidade.TokenVerificacaoEmail;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusContaEnum;
import br.com.joboard.dominio.enums.TipoUsuarioEnum;
import br.com.joboard.dominio.evento.SolicitacaoRedefinicaoSenhaEvento;
import br.com.joboard.dominio.excecao.EmailJaCadastradoException;
import br.com.joboard.dominio.excecao.TokenRedefinicaoInvalidoException;
import br.com.joboard.dominio.excecao.TokenVerificacaoInvalidoException;
import br.com.joboard.repositorio.TipoUsuarioRepositorio;
import br.com.joboard.repositorio.TokenRedefinicaoSenhaRepositorio;
import br.com.joboard.repositorio.TokenVerificacaoEmailRepositorio;
import br.com.joboard.repositorio.UsuarioRepositorio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioServico {

    // Máximo de emails de verificação/redefinição por usuário por hora —
    // barra abuso automatizado e protege a reputação de envio do domínio
    private static final int LIMITE_EMAILS_POR_HORA = 3;
    private static final int MINUTOS_VALIDADE_TOKEN_REDEFINICAO = 30;

    private final UsuarioRepositorio usuarioRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final TokenVerificacaoEmailRepositorio tokenVerificacaoEmailRepositorio;
    private final EmailServico emailServico;
    private final TokenRedefinicaoSenhaRepositorio tokenRedefinicaoSenhaRepositorio;
    private final ApplicationEventPublisher eventPublisher;

    public UsuarioServico(UsuarioRepositorio usuarioRepositorio,
                                   TipoUsuarioRepositorio tipoUsuarioRepositorio,
                                   PasswordEncoder passwordEncoder,
                                   TokenVerificacaoEmailRepositorio tokenVerificacaoEmailRepositorio,
                                   EmailServico emailServico,
                                   TokenRedefinicaoSenhaRepositorio tokenRedefinicaoSenhaRepositorio,
                                   ApplicationEventPublisher eventPublisher) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.tokenVerificacaoEmailRepositorio = tokenVerificacaoEmailRepositorio;
        this.emailServico = emailServico;
        this.tokenRedefinicaoSenhaRepositorio = tokenRedefinicaoSenhaRepositorio;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void cadastrar (CadastroDTO data){
        // Honeypot: campo invisível no formulário — humano nunca preenche.
        // Responde sucesso silencioso para o bot não aprender a contornar.
        if (data.website() != null && !data.website().isBlank()) {
            return;
        }

        if(this.usuarioRepositorio.findByEmail(data.email()).isPresent()){
            throw new EmailJaCadastradoException();
        }

        TipoUsuario tipoUsuario = tipoUsuarioRepositorio.findByNome(TipoUsuarioEnum.USUARIO)
                .orElseThrow(()-> new RuntimeException("Tipo de usuário não encontrado"));

        Usuario novoUsuario = Usuario.builder()
                .email(data.email())
                .senhaHash(passwordEncoder.encode(data.senha()))
                .nome(data.nome())
                .tipoUsuario(List.of(tipoUsuario))
                .statusConta(StatusContaEnum.CADASTRO_PENDENTE_CONFIRMACAO)
                .build();

        this.usuarioRepositorio.save(novoUsuario);

        gerarEnviarToken(novoUsuario);
    }

    @Transactional
    public void registrarLogin(Usuario usuario) {
        usuario.registrarLogin();
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void verificarEmail(String tokenValor){

        TokenVerificacaoEmail token = tokenVerificacaoEmailRepositorio.findByToken(tokenValor)
                .orElseThrow(() -> new TokenVerificacaoInvalidoException("Não foi possível verificar email."));

        if (token.foiUsado()){
            throw new TokenVerificacaoInvalidoException("Este link de verificação já foi utilizado.");
        }

        if (token.estaExpirado()){
            throw new TokenVerificacaoInvalidoException("Link de verificação expirado. Faça login para receber um novo.");
        }

        Usuario usuario = token.getUsuario();
        usuario.setStatusConta(StatusContaEnum.ATIVO);
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuarioRepositorio.save(usuario);

        token.setUsadoEm(LocalDateTime.now());
        tokenVerificacaoEmailRepositorio.save(token);
    }

    @Transactional
    public void reenviarVerificacao (Usuario usuario){
        if (usuario.getStatusConta() == StatusContaEnum.ATIVO) return;
        if (tokenVerificacaoEmailRepositorio.countByUsuarioIdAndCriadoEmAfter(
                usuario.getId(), LocalDateTime.now().minusHours(1)) >= LIMITE_EMAILS_POR_HORA) {
            return; // limite por email atingido — silencioso, o fluxo de conta pendente segue igual
        }
        tokenVerificacaoEmailRepositorio.encontrarPendentePorUsuario(usuario.getId()).ifPresent(
                tokenAntigo -> {
                    tokenAntigo.setUsadoEm(LocalDateTime.now());
                    tokenVerificacaoEmailRepositorio.save(tokenAntigo);
                }
        );
        gerarEnviarToken(usuario);
    }

    @Transactional
    public void reenviarVerificacaoPorEmail(String email) {
        // Chamado por listener de evento: se o usuário não existe mais, não há o que reenviar
        usuarioRepositorio.findByEmail(email).ifPresent(this::reenviarVerificacao);
    }

    @Transactional
    public void gerarEnviarToken (Usuario usuario){

        String tokenvalor = UUID.randomUUID().toString();
        TokenVerificacaoEmail token = TokenVerificacaoEmail.builder()
                .usuario(usuario)
                .token(tokenvalor)
                .expiraEm(LocalDateTime.now().plusHours(2))
                .criadoEm(LocalDateTime.now())
                .build();

        tokenVerificacaoEmailRepositorio.save(token);
        emailServico.enviarVerificacao(usuario.getEmail(), usuario.getNome(), tokenvalor);

    }

    @Transactional
    public void solicitarRedefinicaoSenha(String email) {
        // Sempre silencioso: quem chamou responde a mesma mensagem genérica,
        // exista o email ou não — evita enumeração de usuários
        usuarioRepositorio.findByEmail(email).ifPresent(usuario -> {
            if (tokenRedefinicaoSenhaRepositorio.countByUsuarioIdAndCriadoEmAfter(
                    usuario.getId(), LocalDateTime.now().minusHours(1)) >= LIMITE_EMAILS_POR_HORA) {
                return;
            }

            tokenRedefinicaoSenhaRepositorio.encontrarPendentePorUsuario(usuario.getId()).ifPresent(
                    tokenAntigo -> {
                        tokenAntigo.setUsadoEm(LocalDateTime.now());
                        tokenRedefinicaoSenhaRepositorio.save(tokenAntigo);
                    }
            );

            String tokenValor = UUID.randomUUID().toString();
            TokenRedefinicaoSenha token = TokenRedefinicaoSenha.builder()
                    .usuario(usuario)
                    .token(tokenValor)
                    .expiraEm(LocalDateTime.now().plusMinutes(MINUTOS_VALIDADE_TOKEN_REDEFINICAO))
                    .criadoEm(LocalDateTime.now())
                    .build();
            tokenRedefinicaoSenhaRepositorio.save(token);

            eventPublisher.publishEvent(
                    new SolicitacaoRedefinicaoSenhaEvento(usuario.getEmail(), usuario.getNome(), tokenValor));
        });
    }

    @Transactional
    public void redefinirSenha(String tokenValor, String novaSenha) {
        TokenRedefinicaoSenha token = tokenRedefinicaoSenhaRepositorio.findByToken(tokenValor)
                .orElseThrow(TokenRedefinicaoInvalidoException::new);

        if (token.foiUsado() || token.estaExpirado()) {
            throw new TokenRedefinicaoInvalidoException();
        }

        Usuario usuario = token.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuarioRepositorio.save(usuario);

        token.setUsadoEm(LocalDateTime.now());
        tokenRedefinicaoSenhaRepositorio.save(token);
    }
}
