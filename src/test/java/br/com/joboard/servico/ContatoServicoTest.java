package br.com.joboard.servico;

import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.ContatoRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ownership: contatos são escopados pela candidatura — candidatura de
 * outro usuário → sempre 404, sem tocar nos contatos.
 */
@ExtendWith(MockitoExtension.class)
class ContatoServicoTest {

    @Mock
    private ContatoRepositorio contatoRepositorio;

    @Mock
    private CandidaturaRepositorio candidaturaRepositorio;

    private ContatoServico contatoServico;

    private MockedStatic<SecurityUtils> securityUtils;

    private final Usuario usuarioLogado = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario-a@teste.com")
            .build();

    @BeforeEach
    void setUp() {
        contatoServico = new ContatoServico(contatoRepositorio, candidaturaRepositorio);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getUsuarioLogado).thenReturn(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void listar_candidaturaDeOutroUsuario_deveLancar404() {
        UUID candidaturaDeOutroUsuario = UUID.randomUUID();
        when(candidaturaRepositorio.findByIdAndUsuarioId(candidaturaDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contatoServico.listar(candidaturaDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(contatoRepositorio, never()).findAllByCandidaturaId(any());
    }

    @Test
    void deletar_candidaturaDeOutroUsuario_deveLancar404() {
        UUID candidaturaDeOutroUsuario = UUID.randomUUID();
        UUID contatoId = UUID.randomUUID();
        when(candidaturaRepositorio.findByIdAndUsuarioId(candidaturaDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contatoServico.deletar(candidaturaDeOutroUsuario, contatoId))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(contatoRepositorio, never()).delete(any());
    }
}
