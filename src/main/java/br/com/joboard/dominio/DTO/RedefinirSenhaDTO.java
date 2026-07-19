package br.com.joboard.dominio.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(
        @NotBlank(message = "Token obrigatório") String token,
        @NotBlank(message = "Senha obrigatória") @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres") String novaSenha
) {
}
