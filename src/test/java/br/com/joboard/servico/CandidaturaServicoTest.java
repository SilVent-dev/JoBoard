package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.AtualizarStatusDTO;
import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.entidade.EmpresaCatalogada;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.entidade.VagaRastreada;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.dominio.excecao.ResultadoFinalNaoDefinidoException;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.CurriculoRepositorio;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidaturaServicoTest {

    @Mock
    private CandidaturaRepositorio candidaturaRepositorio;
    @Mock
    private VagaRepositorio vagaRepositorio;
    @Mock
    private CurriculoRepositorio curriculoRepositorio;
    @Mock
    private HistoricoServico historicoServico;

    private CandidaturaServico candidaturaServico;

    private MockedStatic<SecurityUtils> securityUtils;

    private final Usuario usuarioLogado = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario-a@teste.com")
            .build();

    @BeforeEach
    void setUp() {
        candidaturaServico = new CandidaturaServico(
                candidaturaRepositorio, vagaRepositorio, curriculoRepositorio, historicoServico);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getUsuarioLogado).thenReturn(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void arquivar_semResultadoFinal_deveLancarRegraDeNegocio() {
        UUID id = UUID.randomUUID();
        Candidatura semResultado = Candidatura.builder().id(id).build();
        when(candidaturaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId()))
                .thenReturn(Optional.of(semResultado));

        assertThatThrownBy(() -> candidaturaServico.arquivar(id))
                .isInstanceOf(ResultadoFinalNaoDefinidoException.class);

        verify(candidaturaRepositorio, never()).save(any());
    }

    @Test
    void arquivar_candidaturaDeOutroUsuario_deveLancar404() {
        UUID idDeOutroUsuario = UUID.randomUUID();
        when(candidaturaRepositorio.findByIdAndUsuarioId(idDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidaturaServico.arquivar(idDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ── Gatilho de próxima ação na mudança de status ──────────────────

    private Candidatura candidaturaEm(StatusProcessoSeletivoEnum status) {
        VagaRastreada vaga = VagaRastreada.builder()
                .id(UUID.randomUUID())
                .titulo("Dev Backend")
                .empresa(EmpresaCatalogada.builder().id(UUID.randomUUID()).nome("Acme").build())
                .build();
        return Candidatura.builder()
                .id(UUID.randomUUID())
                .usuario(usuarioLogado)
                .vaga(vaga)
                .status(status)
                .build();
    }

    private Candidatura prepararAtualizacao(Candidatura candidatura) {
        when(candidaturaRepositorio.findByIdAndUsuarioId(candidatura.getId(), usuarioLogado.getId()))
                .thenReturn(Optional.of(candidatura));
        when(candidaturaRepositorio.save(any())).thenAnswer(inv -> inv.getArgument(0));
        return candidatura;
    }

    @Test
    void atualizarStatus_semAcaoExplicita_aplicaSugestaoDoMapa() {
        Candidatura candidatura = prepararAtualizacao(
                candidaturaEm(StatusProcessoSeletivoEnum.LISTA_DESEJO));

        candidaturaServico.atualizarStatus(candidatura.getId(),
                new AtualizarStatusDTO(StatusProcessoSeletivoEnum.APLICADA, null, null, null));

        assertThat(candidatura.getProximaAcaoDescricao()).isEqualTo("Fazer follow-up da aplicação");
        assertThat(candidatura.getProximaAcaoEm()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void atualizarStatus_comAcaoExplicita_naoSobrescreveComSugestao() {
        Candidatura candidatura = prepararAtualizacao(
                candidaturaEm(StatusProcessoSeletivoEnum.LISTA_DESEJO));
        LocalDate dataManual = LocalDate.now().plusDays(14);

        candidaturaServico.atualizarStatus(candidatura.getId(),
                new AtualizarStatusDTO(StatusProcessoSeletivoEnum.APLICADA, null,
                        dataManual, "Ligar para o recrutador"));

        assertThat(candidatura.getProximaAcaoDescricao()).isEqualTo("Ligar para o recrutador");
        assertThat(candidatura.getProximaAcaoEm()).isEqualTo(dataManual);
    }

    @Test
    void atualizarStatus_paraStatusFinal_limpaProximaAcao() {
        Candidatura candidatura = candidaturaEm(StatusProcessoSeletivoEnum.PROPOSTA_RECEBIDA);
        candidatura.setProximaAcaoEm(LocalDate.now().plusDays(2));
        candidatura.setProximaAcaoDescricao("Responder a proposta");
        prepararAtualizacao(candidatura);

        candidaturaServico.atualizarStatus(candidatura.getId(),
                new AtualizarStatusDTO(StatusProcessoSeletivoEnum.ACEITA, null, null, null));

        assertThat(candidatura.getProximaAcaoEm()).isNull();
        assertThat(candidatura.getProximaAcaoDescricao()).isNull();
    }

    @Test
    void buscarPorId_candidaturaDeOutroUsuario_deveLancar404() {
        UUID idDeOutroUsuario = UUID.randomUUID();
        when(candidaturaRepositorio.findByIdAndUsuarioId(idDeOutroUsuario, usuarioLogado.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidaturaServico.buscarPorId(idDeOutroUsuario))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
