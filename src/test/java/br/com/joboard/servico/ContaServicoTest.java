package br.com.joboard.servico;

import br.com.joboard.dominio.entidade.Curriculo;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.SenhaIncorretaException;
import br.com.joboard.repositorio.*;
import br.com.joboard.seguranca.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exclusão de conta (LGPD): exige a senha atual; remove arquivos do Storage
 * ANTES do banco (se o Storage falhar, a conta permanece intacta); apaga
 * tudo na ordem inversa das FKs.
 */
@ExtendWith(MockitoExtension.class)
class ContaServicoTest {

    @Mock private UsuarioRepositorio usuarioRepositorio;
    @Mock private PerfilCandidatoRepositorio perfilCandidatoRepositorio;
    @Mock private CurriculoRepositorio curriculoRepositorio;
    @Mock private EmpresaRepositorio empresaRepositorio;
    @Mock private VagaRepositorio vagaRepositorio;
    @Mock private CandidaturaRepositorio candidaturaRepositorio;
    @Mock private HistoricoRepositorio historicoRepositorio;
    @Mock private ContatoRepositorio contatoRepositorio;
    @Mock private TokenVerificacaoEmailRepositorio tokenVerificacaoEmailRepositorio;
    @Mock private TokenRedefinicaoSenhaRepositorio tokenRedefinicaoSenhaRepositorio;
    @Mock private StorageServico storageServico;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private ContaServico contaServico;

    private MockedStatic<SecurityUtils> securityUtils;

    private final Usuario usuario = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario@teste.com")
            .senhaHash("$2a$hash")
            .build();

    @BeforeEach
    void setUp() {
        contaServico = new ContaServico(usuarioRepositorio, perfilCandidatoRepositorio,
                curriculoRepositorio, empresaRepositorio, vagaRepositorio, candidaturaRepositorio,
                historicoRepositorio, contatoRepositorio, tokenVerificacaoEmailRepositorio,
                tokenRedefinicaoSenhaRepositorio, storageServico, passwordEncoder);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getUsuarioLogado).thenReturn(usuario);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void excluir_comSenhaErrada_naoDeletaNada() {
        when(passwordEncoder.matches("senha-errada", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> contaServico.excluirConta("senha-errada"))
                .isInstanceOf(SenhaIncorretaException.class);

        verify(storageServico, never()).deletar(any());
        verify(usuarioRepositorio, never()).delete(any());
        verify(candidaturaRepositorio, never()).deletarTodosDoUsuario(any());
    }

    @Test
    void excluir_comSenhaCorreta_removeStorageAntesDoBancoENaOrdemDasFks() {
        when(passwordEncoder.matches("senha-certa", "$2a$hash")).thenReturn(true);
        Curriculo curriculo = Curriculo.builder()
                .usuario(usuario)
                .urlArquivo("https://storage/curriculos/abc.pdf")
                .build();
        when(curriculoRepositorio.findAllByUsuarioId(usuario.getId()))
                .thenReturn(List.of(curriculo));

        contaServico.excluirConta("senha-certa");

        InOrder ordem = inOrder(storageServico, contatoRepositorio, historicoRepositorio,
                candidaturaRepositorio, vagaRepositorio, empresaRepositorio,
                curriculoRepositorio, perfilCandidatoRepositorio, usuarioRepositorio);

        ordem.verify(storageServico).deletar("https://storage/curriculos/abc.pdf");
        ordem.verify(contatoRepositorio).deletarTodosDoUsuario(usuario.getId());
        ordem.verify(historicoRepositorio).deletarTodosDoUsuario(usuario.getId());
        ordem.verify(candidaturaRepositorio).deletarTodosDoUsuario(usuario.getId());
        ordem.verify(vagaRepositorio).deletarTodosDoUsuario(usuario.getId());
        ordem.verify(empresaRepositorio).deletarTodosDoUsuario(usuario.getId());
        ordem.verify(curriculoRepositorio).deletarTodosDoUsuario(usuario.getId());
        ordem.verify(perfilCandidatoRepositorio).deletarDoUsuario(usuario.getId());
        ordem.verify(usuarioRepositorio).delete(usuario);
    }

    @Test
    void excluir_seStorageFalhar_bancoNaoETocado() {
        when(passwordEncoder.matches("senha-certa", "$2a$hash")).thenReturn(true);
        Curriculo curriculo = Curriculo.builder()
                .usuario(usuario)
                .urlArquivo("https://storage/curriculos/abc.pdf")
                .build();
        when(curriculoRepositorio.findAllByUsuarioId(usuario.getId()))
                .thenReturn(List.of(curriculo));
        Mockito.doThrow(new RuntimeException("storage indisponível"))
                .when(storageServico).deletar(any());

        assertThatThrownBy(() -> contaServico.excluirConta("senha-certa"))
                .isInstanceOf(RuntimeException.class);

        verify(usuarioRepositorio, never()).delete(any());
        verify(candidaturaRepositorio, never()).deletarTodosDoUsuario(any());
    }
}
