package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.CadastroDTO;
import br.com.joboard.dominio.entidade.TipoUsuario;
import br.com.joboard.dominio.entidade.TokenVerificacaoEmail;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusContaEnum;
import br.com.joboard.dominio.enums.TipoUsuarioEnum;
import br.com.joboard.dominio.excecao.EmailJaCadastradoException;
import br.com.joboard.repositorio.TipoUsuarioRepositorio;
import br.com.joboard.repositorio.TokenVerificacaoEmailRepositorio;
import br.com.joboard.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final TokenVerificacaoEmailRepositorio tokenVerificacaoEmailRepositorio;
    private final EmailServico emailServico;

    public UsuarioServico(UsuarioRepositorio usuarioRepositorio,
                                   TipoUsuarioRepositorio tipoUsuarioRepositorio,
                                   PasswordEncoder passwordEncoder,
                                   TokenVerificacaoEmailRepositorio tokenVerificacaoEmailRepositorio,
                                   EmailServico emailServico) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.tokenVerificacaoEmailRepositorio = tokenVerificacaoEmailRepositorio;
        this.emailServico = emailServico;
    }

    @Transactional
    public void cadastrar (CadastroDTO data){
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
                .orElseThrow(() -> new IllegalArgumentException("Não foi possível verificar email."));

        if (token.foiUsado()){
            throw new IllegalStateException("Este link de verificação já foi utilizado.");
        }

        if (token.estaExpirado()){
            throw new IllegalStateException("Link de verificação expirado. Faça login para receber um novo.");
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
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        reenviarVerificacao(usuario);
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
}
