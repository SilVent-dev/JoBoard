package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.TipoContatoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContatoRequestDTO(

        @NotBlank(message = "Nome do contato é obrigatório")
        String nome,

        String cargo,
        String email,
        String telefone,
        String linkedin,

        @NotNull(message = "Tipo do contato é obrigatório")
        TipoContatoEnum tipoContato,

        String interacaoPrincipal
) {}