package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import jakarta.validation.constraints.NotNull;


public record AtualizarStatusDTO(
        @NotNull(message = "Novo status é obrigatório")
        StatusProcessoSeletivoEnum novoStatus,

        String descricao
) {

}
