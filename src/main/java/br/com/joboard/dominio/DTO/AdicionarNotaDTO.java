package br.com.joboard.dominio.DTO;

import jakarta.validation.constraints.NotBlank;

public record AdicionarNotaDTO(
        @NotBlank(message = "Nota não pode ser vazia")
        String nota
) {

}
