package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CandidaturaResponseDTO(UUID id,
                                     UUID vagaId,
                                     String vagaTitulo,
                                     String empresaNome,
                                     UUID curriculoId,
                                     StatusProcessoSeletivoEnum status,
                                     LocalDate dataAplicacao,
                                     PlataformaAplicacaoEnum plataformaAplicacao,
                                     String cartaApresentacao,
                                     String portfolioEnviado,
                                     LocalDate proximaAcaoEm,
                                     String proximaAcaoDescricao,
                                     BigDecimal minhaAvaliacaoInteresse,
                                     BigDecimal minhaAvaliacaoFit,
                                     String notas,
                                     ResultadoFinalEnum resultadoFinal,
                                     String feedbackRecebido,
                                     String motivoRejeicao,
                                     boolean arquivada,
                                     LocalDateTime arquivadaEm,
                                     LocalDateTime criadoEm,
                                     LocalDateTime atualizadoEm) {
    public static CandidaturaResponseDTO from(Candidatura c) {
        return new CandidaturaResponseDTO(
                c.getId(),
                c.getVaga().getId(),
                c.getVaga().getTitulo(),
                c.getVaga().getEmpresa().getNome(),
                c.getCurriculo() != null ? c.getCurriculo().getId() : null,
                c.getStatus(),
                c.getDataAplicacao(),
                c.getPlataformaAplicacao(),
                c.getCartaApresentacao(),
                c.getPortfolioEnviado(),
                c.getProximaAcaoEm(),
                c.getProximaAcaoDescricao(),
                c.getMinhaAvaliacaoInteresse(),
                c.getMinhaAvaliacaoFit(),
                c.getNotas(),
                c.getResultadoFinal(),
                c.getFeedbackRecebido(),
                c.getMotivoRejeicao(),
                c.isArquivada(),
                c.getArquivadaEm(),
                c.getCriadoEm(),
                c.getAtualizadoEm()
        );
    }
}
