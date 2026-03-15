package br.com.joboard.dominio.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AutenticacaDTO(
        @NotBlank @Email String email,
        @NotBlank String senha) {
}
