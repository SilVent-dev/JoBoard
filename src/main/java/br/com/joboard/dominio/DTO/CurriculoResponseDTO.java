package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.entidade.Curriculo;

import java.time.LocalDateTime;
import java.util.UUID;

public record CurriculoResponseDTO(UUID id,
                                   String nomeArquivo,
                                   String urlArquivo,
                                   Long tamanhoBytes,
                                   String tipoMime,
                                   String versao,
                                   boolean ehPrincipal,
                                   LocalDateTime criadoEm) {
    public static CurriculoResponseDTO from(Curriculo curriculo) {
        return new CurriculoResponseDTO(
                curriculo.getId(),
                curriculo.getNomeArquivo(),
                curriculo.getUrlArquivo(),
                curriculo.getTamanhoBytes(),
                curriculo.getTipoMime(),
                curriculo.getVersao(),
                curriculo.isEhPrincipal(),
                curriculo.getCriadoEm()
        );
    }
}
