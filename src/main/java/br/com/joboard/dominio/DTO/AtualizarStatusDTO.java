package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record AtualizarStatusDTO(
        @NotNull(message = "Novo status é obrigatório")
        StatusProcessoSeletivoEnum novoStatus,

        String descricao,

        // Próxima ação explícita (opcional) — quando ausente, o service sugere
        // uma automaticamente conforme o novo status
        LocalDate proximaAcaoEm,
        String proximaAcaoDescricao
) {

}
