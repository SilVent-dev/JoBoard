package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.InsightsResponseDTO;
import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.entidade.HistoricoCandidatura;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import br.com.joboard.dominio.enums.TipoEventoEnum;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.HistoricoRepositorio;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Insights derivados do histórico: funil por etapa atingida, taxa de
 * resposta (rejeição conta como resposta; desistência não) e tempo médio.
 */
@ExtendWith(MockitoExtension.class)
class InsightServicoTest {

    @Mock
    private HistoricoRepositorio historicoRepositorio;
    @Mock
    private CandidaturaRepositorio candidaturaRepositorio;

    private InsightServico insightServico;

    private MockedStatic<SecurityUtils> securityUtils;

    private final Usuario usuarioLogado = Usuario.builder()
            .id(UUID.randomUUID())
            .email("usuario@teste.com")
            .build();

    @BeforeEach
    void setUp() {
        insightServico = new InsightServico(historicoRepositorio, candidaturaRepositorio);
        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getUsuarioLogado).thenReturn(usuarioLogado);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    private HistoricoCandidatura evento(Candidatura candidatura,
                                        StatusProcessoSeletivoEnum statusNovo,
                                        LocalDateTime registradoEm) {
        return HistoricoCandidatura.builder()
                .id(UUID.randomUUID())
                .candidatura(candidatura)
                .tipoEvento(TipoEventoEnum.MUDANCA_STATUS)
                .statusNovo(statusNovo)
                .tituloEvento("Status alterado")
                .dataEvento(LocalDate.now())
                .registradoEm(registradoEm)
                .build();
    }

    @Test
    void calculaFunilTaxaDeRespostaETempoMedio() {
        Candidatura respondida = Candidatura.builder().id(UUID.randomUUID()).build();
        Candidatura semResposta = Candidatura.builder().id(UUID.randomUUID()).build();
        LocalDateTime base = LocalDateTime.of(2026, 7, 1, 10, 0);

        when(historicoRepositorio.findAllByCandidaturaUsuarioId(usuarioLogado.getId()))
                .thenReturn(List.of(
                        // respondida: wishlist → aplicada (2 dias depois) → triagem (4 dias depois)
                        evento(respondida, StatusProcessoSeletivoEnum.LISTA_DESEJO, base),
                        evento(respondida, StatusProcessoSeletivoEnum.APLICADA, base.plusDays(2)),
                        evento(respondida, StatusProcessoSeletivoEnum.TRIAGEM_TELEFONICA, base.plusDays(6)),
                        // sem resposta: wishlist → aplicada, parada desde então
                        evento(semResposta, StatusProcessoSeletivoEnum.LISTA_DESEJO, base),
                        evento(semResposta, StatusProcessoSeletivoEnum.APLICADA, base.plusDays(1))
                ));

        InsightsResponseDTO insights = insightServico.gerar();

        assertThat(insights.totalCandidaturas()).isEqualTo(2);
        assertThat(insights.totalAplicadas()).isEqualTo(2);
        // 1 de 2 aplicadas teve resposta (chegou à triagem)
        assertThat(insights.taxaRespostaPercentual()).isEqualTo(50.0);

        // Funil: 2 na wishlist, 2 aplicadas, 1 na triagem
        assertThat(insights.funil())
                .filteredOn(e -> e.status() == StatusProcessoSeletivoEnum.LISTA_DESEJO)
                .first().satisfies(e -> assertThat(e.quantidade()).isEqualTo(2));
        assertThat(insights.funil())
                .filteredOn(e -> e.status() == StatusProcessoSeletivoEnum.TRIAGEM_TELEFONICA)
                .first().satisfies(e -> assertThat(e.quantidade()).isEqualTo(1));

        // Tempo médio em APLICADA: só a respondida fechou o intervalo (4 dias)
        assertThat(insights.temposMedios())
                .filteredOn(t -> t.status() == StatusProcessoSeletivoEnum.APLICADA)
                .first().satisfies(t -> assertThat(t.diasMedios()).isEqualTo(4.0));
    }

    @Test
    void semHistorico_retornaZerosSemErro() {
        when(historicoRepositorio.findAllByCandidaturaUsuarioId(usuarioLogado.getId()))
                .thenReturn(List.of());

        InsightsResponseDTO insights = insightServico.gerar();

        assertThat(insights.totalCandidaturas()).isZero();
        assertThat(insights.taxaRespostaPercentual()).isZero();
        assertThat(insights.temposMedios()).isEmpty();
    }
}
