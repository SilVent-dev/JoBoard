package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.InsightsResponseDTO;
import br.com.joboard.dominio.DTO.InsightsResponseDTO.EtapaFunilDTO;
import br.com.joboard.dominio.DTO.InsightsResponseDTO.TempoStatusDTO;
import br.com.joboard.dominio.entidade.HistoricoCandidatura;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import br.com.joboard.dominio.enums.TipoEventoEnum;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.HistoricoRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Painel de insights derivado inteiramente do HistoricoCandidatura —
 * nenhuma fonte de dado nova.
 */
@Service
@RequiredArgsConstructor
public class InsightServico {

    // Etapas do funil na ordem de avanço do pipeline
    private static final List<StatusProcessoSeletivoEnum> ETAPAS_FUNIL = List.of(
            StatusProcessoSeletivoEnum.LISTA_DESEJO,
            StatusProcessoSeletivoEnum.APLICADA,
            StatusProcessoSeletivoEnum.TRIAGEM_TELEFONICA,
            StatusProcessoSeletivoEnum.ENTREVISTA_TECNICA,
            StatusProcessoSeletivoEnum.ENTREVISTA_COMPORTAMENTAL,
            StatusProcessoSeletivoEnum.TESTE_PRATICO,
            StatusProcessoSeletivoEnum.PROPOSTA_RECEBIDA,
            StatusProcessoSeletivoEnum.ACEITA
    );

    // "Teve resposta" = saiu de APLICADA para qualquer etapa seguinte ou foi
    // rejeitada (rejeição é resposta); desistência não conta
    private static final Set<StatusProcessoSeletivoEnum> STATUS_DE_RESPOSTA = EnumSet.of(
            StatusProcessoSeletivoEnum.TRIAGEM_TELEFONICA,
            StatusProcessoSeletivoEnum.ENTREVISTA_TECNICA,
            StatusProcessoSeletivoEnum.ENTREVISTA_COMPORTAMENTAL,
            StatusProcessoSeletivoEnum.TESTE_PRATICO,
            StatusProcessoSeletivoEnum.PROPOSTA_RECEBIDA,
            StatusProcessoSeletivoEnum.ACEITA,
            StatusProcessoSeletivoEnum.REJEITADA
    );

    private final HistoricoRepositorio historicoRepositorio;
    private final CandidaturaRepositorio candidaturaRepositorio;

    @Transactional(readOnly = true)
    public InsightsResponseDTO gerar() {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        List<HistoricoCandidatura> mudancas = historicoRepositorio
                .findAllByCandidaturaUsuarioId(usuarioLogado.getId())
                .stream()
                .filter(h -> h.getTipoEvento() == TipoEventoEnum.MUDANCA_STATUS)
                .toList();

        Map<UUID, List<HistoricoCandidatura>> porCandidatura = mudancas.stream()
                .collect(Collectors.groupingBy(h -> h.getCandidatura().getId()));

        return new InsightsResponseDTO(
                porCandidatura.size(),
                (int) contarQueAtingiram(porCandidatura, StatusProcessoSeletivoEnum.APLICADA),
                calcularTaxaResposta(porCandidatura),
                montarFunil(porCandidatura),
                calcularTemposMedios(porCandidatura)
        );
    }

    private long contarQueAtingiram(Map<UUID, List<HistoricoCandidatura>> porCandidatura,
                                    StatusProcessoSeletivoEnum status) {
        return porCandidatura.values().stream()
                .filter(eventos -> eventos.stream().anyMatch(h -> h.getStatusNovo() == status))
                .count();
    }

    private List<EtapaFunilDTO> montarFunil(Map<UUID, List<HistoricoCandidatura>> porCandidatura) {
        return ETAPAS_FUNIL.stream()
                .map(status -> new EtapaFunilDTO(status, status.getDescricao(),
                        contarQueAtingiram(porCandidatura, status)))
                .toList();
    }

    private double calcularTaxaResposta(Map<UUID, List<HistoricoCandidatura>> porCandidatura) {
        long aplicadas = contarQueAtingiram(porCandidatura, StatusProcessoSeletivoEnum.APLICADA);
        if (aplicadas == 0) {
            return 0.0;
        }
        long respondidas = porCandidatura.values().stream()
                .filter(eventos -> eventos.stream()
                        .anyMatch(h -> h.getStatusNovo() == StatusProcessoSeletivoEnum.APLICADA))
                .filter(eventos -> eventos.stream()
                        .anyMatch(h -> STATUS_DE_RESPOSTA.contains(h.getStatusNovo())))
                .count();
        return Math.round(respondidas * 1000.0 / aplicadas) / 10.0;
    }

    // Tempo em um status = intervalo entre o evento que entrou nele e o evento
    // seguinte da mesma candidatura; o status atual (em aberto) não entra na média
    private List<TempoStatusDTO> calcularTemposMedios(
            Map<UUID, List<HistoricoCandidatura>> porCandidatura) {

        Map<StatusProcessoSeletivoEnum, List<Long>> horasPorStatus =
                new EnumMap<>(StatusProcessoSeletivoEnum.class);

        for (List<HistoricoCandidatura> eventos : porCandidatura.values()) {
            List<HistoricoCandidatura> ordenados = eventos.stream()
                    .sorted(Comparator.comparing(HistoricoCandidatura::getRegistradoEm))
                    .toList();
            for (int i = 0; i < ordenados.size() - 1; i++) {
                long horas = Duration.between(
                        ordenados.get(i).getRegistradoEm(),
                        ordenados.get(i + 1).getRegistradoEm()).toHours();
                horasPorStatus
                        .computeIfAbsent(ordenados.get(i).getStatusNovo(), s -> new java.util.ArrayList<>())
                        .add(horas);
            }
        }

        return horasPorStatus.entrySet().stream()
                .map(entrada -> {
                    double mediaHoras = entrada.getValue().stream()
                            .mapToLong(Long::longValue).average().orElse(0);
                    return new TempoStatusDTO(entrada.getKey(), entrada.getKey().getDescricao(),
                            Math.round(mediaHoras / 24.0 * 10.0) / 10.0);
                })
                .sorted(Comparator.comparingInt(t -> ETAPAS_FUNIL.indexOf(t.status())))
                .toList();
    }
}
