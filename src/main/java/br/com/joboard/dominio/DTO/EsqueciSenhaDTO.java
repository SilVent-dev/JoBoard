package br.com.joboard.dominio.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaDTO(
        @NotBlank(message = "Email obrigatório") @Email(message = "Email inválido") String email
) {
}
