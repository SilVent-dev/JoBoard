package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.ModeloTrabalhoEnum;
import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoContratoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;


public record VagaRequestDTO(@NotNull(message = "Empresa é obrigatória")
                             UUID empresaId,

                             @NotBlank(message = "Título da vaga é obrigatório")
                             @Size(max = 255)
                             String titulo,

                             String descricao,

                             @Size(max = 500)
                             String urlVaga,

                             String localizacao,
                             ModeloTrabalhoEnum modeloTrabalho,
                             TipoContratoEnum tipoContrato,
                             NivelExperienciaEnum nivelExperiencia,
                             String faixaSalarial,
                             String beneficios,
                             String requisitosObrigatorios,
                             String requisitosDesejaveis) {

}
