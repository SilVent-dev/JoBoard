package br.com.joboard.servico;

import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.CurriculoRepositorio;
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
 * Ownership: currículo inexistente ou de outro usuário → sempre 404,
 * e nada é deletado do Storage nem do banco.
 */
@ExtendWith(MockitoExtension.class)
class CurriculoServicoTest {

    @Mock
    private CurriculoRepositorio curriculoRepositorio;

    @Mock
    private StorageServico storageServico;

    private CurriculoServico curriculoServico;

    private MockedStatic<SecurityUtils> securityUtils;

    private final Usuario usuarioLogado = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario-a@teste.com")
            .build();

    @BeforeEach
    void setUp() {
        curriculoServico = new CurriculoServico(curriculoRepositorio, storageServico);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getUsuarioLogado).thenReturn(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void marcarComoPrincipal_curriculoDeOutroUsuario_deveLancar404() {
        UUID idDeOutroUsuario = UUID.randomUUID();
        when(curriculoRepositorio.findByIdAndUsuarioId(idDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> curriculoServico.marcarComoPrincipal(idDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(curriculoRepositorio, never()).desmarcarTodosPrincipais(any());
    }

    @Test
    void deletar_curriculoDeOutroUsuario_deveLancar404SemTocarStorage() {
        UUID idDeOutroUsuario = UUID.randomUUID();
        when(curriculoRepositorio.findByIdAndUsuarioId(idDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> curriculoServico.deletar(idDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(storageServico, never()).deletar(any());
        verify(curriculoRepositorio, never()).delete(any());
    }
}
