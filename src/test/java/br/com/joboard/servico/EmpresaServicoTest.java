package br.com.joboard.servico;

import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.EmpresaRepositorio;
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
 * Ownership: empresa inexistente ou de outro usuário → sempre 404.
 */
@ExtendWith(MockitoExtension.class)
class EmpresaServicoTest {

    @Mock
    private EmpresaRepositorio empresaRepositorio;

    private EmpresaServico empresaServico;

    private MockedStatic<SecurityUtils> securityUtils;

    private final Usuario usuarioLogado = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario-a@teste.com")
            .build();

    @BeforeEach
    void setUp() {
        empresaServico = new EmpresaServico(empresaRepositorio);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getUsuarioLogado).thenReturn(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void buscarPorId_empresaDeOutroUsuario_deveLancar404() {
        UUID idDeOutroUsuario = UUID.randomUUID();
        when(empresaRepositorio.findByIdAndUsuarioId(idDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaServico.buscarPorId(idDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deletar_empresaDeOutroUsuario_deveLancar404() {
        UUID idDeOutroUsuario = UUID.randomUUID();
        when(empresaRepositorio.findByIdAndUsuarioId(idDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaServico.deletar(idDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(empresaRepositorio, never()).delete(any());
    }
}
