package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;

import java.util.List;

public record InsightsResponseDTO(
        int totalCandidaturas,
        int totalAplicadas,
        double taxaRespostaPercentual,
        List<EtapaFunilDTO> funil,
        List<TempoStatusDTO> temposMedios
) {
    public record EtapaFunilDTO(
            StatusProcessoSeletivoEnum status,
            String descricao,
            long quantidade
    ) {}

    public record TempoStatusDTO(
            StatusProcessoSeletivoEnum status,
            String descricao,
            double diasMedios
    ) {}
}
