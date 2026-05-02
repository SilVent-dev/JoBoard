package br.com.joboard.dominio.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CurriculoVersaoDTO(@NotBlank(message = "Versão obrigatória")
                                 @Size(max = 100, message = "Versão deve ter no máximo 100 caracteres")
                                 String versao) {
}
