package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.VagaRequestDTO;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.entidade.VagaRastreada;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.EmpresaRepositorio;
import br.com.joboard.repositorio.VagaRepositorio;
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
 * Regra de ouro de ownership: recurso inexistente OU de outro usuário
 * retorna sempre 404 (RecursoNaoEncontradoException) — nunca 403,
 * para não revelar que o recurso existe.
 */
@ExtendWith(MockitoExtension.class)
class VagaServicoTest {

    @Mock
    private VagaRepositorio vagaRepositorio;

    @Mock
    private EmpresaRepositorio empresaRepositorio;

    private VagaServico vagaServico;

    private MockedStatic<SecurityUtils> securityUtils;

    private final Usuario usuarioLogado = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario-a@teste.com")
            .build();

    @BeforeEach
    void setUp() {
        vagaServico = new VagaServico(vagaRepositorio, empresaRepositorio);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getUsuarioLogado).thenReturn(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    private VagaRequestDTO requestComEmpresa(UUID empresaId) {
        return new VagaRequestDTO(empresaId, "Dev Backend", null, null, null,
                null, null, null, null, null, null, null);
    }

    @Test
    void criar_comEmpresaDeOutroUsuario_deveLancar404() {
        UUID empresaDeOutroUsuario = UUID.randomUUID();
        when(empresaRepositorio.findByIdAndUsuarioId(empresaDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vagaServico.criar(requestComEmpresa(empresaDeOutroUsuario)))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(vagaRepositorio, never()).save(any());
    }

    @Test
    void atualizar_comEmpresaDeOutroUsuario_deveLancar404() {
        UUID vagaId = UUID.randomUUID();
        UUID empresaDeOutroUsuario = UUID.randomUUID();

        when(vagaRepositorio.findByIdAndUsuarioId(vagaId, usuarioLogado.getId()))
                .thenReturn(Optional.of(VagaRastreada.builder().id(vagaId).build()));
        when(empresaRepositorio.findByIdAndUsuarioId(empresaDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vagaServico.atualizar(vagaId, requestComEmpresa(empresaDeOutroUsuario)))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(vagaRepositorio, never()).save(any());
    }

    @Test
    void buscarPorId_vagaDeOutroUsuario_deveLancar404() {
        UUID vagaDeOutroUsuario = UUID.randomUUID();
        when(vagaRepositorio.findByIdAndUsuarioId(vagaDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vagaServico.buscarPorId(vagaDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
