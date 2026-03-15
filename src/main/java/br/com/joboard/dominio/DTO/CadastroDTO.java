package br.com.joboard.dominio.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroDTO(
        @NotBlank(message = "Nome obrigatório") String nome,
        @NotBlank(message = "Email obrigatório") @Email(message = "Email inválido") String email,
        @NotBlank(message = "Senha obrigatória") @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres") String senha
) {
}
