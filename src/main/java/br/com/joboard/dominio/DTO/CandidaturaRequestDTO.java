package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.PlataformaAplicacaoEnum;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public record CandidaturaRequestDTO(
        @NotNull(message = "Vaga é obrigatória")
        UUID vagaId,

        UUID curriculoId,

        PlataformaAplicacaoEnum plataformaAplicacao,
        LocalDate dataAplicacao,
        String cartaApresentacao,
        String portfolioEnviado,
        LocalDate proximaAcaoEm,
        String proximaAcaoDescricao,

        @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
        BigDecimal minhaAvaliacaoInteresse,

        @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
        BigDecimal minhaAvaliacaoFit,

        String notas
) {

}
