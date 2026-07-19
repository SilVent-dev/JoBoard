package br.com.joboard.dominio.DTO;

import jakarta.validation.constraints.NotBlank;

public record ExcluirContaDTO(
        @NotBlank(message = "Senha obrigatória") String senha
) {
}
