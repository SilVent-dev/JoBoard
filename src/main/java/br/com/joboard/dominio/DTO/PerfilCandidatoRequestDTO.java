package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoDisponibilidadeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;

public record PerfilCandidatoRequestDTO(
        @NotBlank(message = "Nome completo obrigatório")
        String nomeCompleto,

        @NotBlank(message = "CPF obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @Size(max = 20)
        String telefone,

        @Size(max = 100)
        String cidade,

        @Size(min = 2, max = 2, message = "Estado deve ter exatamente 2 caracteres (UF)")
        String estado,

        Boolean aceitaRemoto,
        Boolean aceitaHibrido,
        Boolean aceitaPresencial,

        NivelExperienciaEnum nivelExperiencia,
        TipoDisponibilidadeEnum disponibilidade,

        @Positive
        BigDecimal pretensaoSalarialMin,

        @Positive
        BigDecimal pretensaoSalarialMax,

        @Size(max = 255)
        String urlLinkedin,

        @Size(max = 255)
        String urlGithub,

        @Size(max = 255)
        String urlPortfolio,

        @Size(max = 500)
        String resumoProfissional

) {
}
