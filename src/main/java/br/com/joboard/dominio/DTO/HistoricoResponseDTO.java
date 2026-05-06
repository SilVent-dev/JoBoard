package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.entidade.HistoricoCandidatura;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import br.com.joboard.dominio.enums.TipoEventoEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoResponseDTO(UUID id,
                                   TipoEventoEnum tipoEvento,
                                   StatusProcessoSeletivoEnum statusAnterior,
                                   StatusProcessoSeletivoEnum statusNovo,
                                   String tituloEvento,
                                   String descricao,
                                   LocalDate dataEvento,
                                   LocalDateTime registradoEm) {
    public static HistoricoResponseDTO from(HistoricoCandidatura h) {
        return new HistoricoResponseDTO(
                h.getId(),
                h.getTipoEvento(),
                h.getStatusAnterior(),
                h.getStatusNovo(),
                h.getTituloEvento(),
                h.getDescricao(),
                h.getDataEvento(),
                h.getRegistradoEm()
        );
    }
}
