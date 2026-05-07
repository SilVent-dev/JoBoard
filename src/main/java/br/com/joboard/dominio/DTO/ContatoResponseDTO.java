package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.entidade.Contato;
import br.com.joboard.dominio.enums.TipoContatoEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContatoResponseDTO(
        UUID id,
        UUID candidaturaId,
        String nome,
        String cargo,
        String email,
        String telefone,
        String linkedin,
        TipoContatoEnum tipoContato,
        String interacaoPrincipal,
        LocalDateTime criadoEm
) {
        public static ContatoResponseDTO from(Contato contato) {
                return new ContatoResponseDTO(
                        contato.getId(),
                        contato.getCandidatura().getId(),
                        contato.getNome(),
                        contato.getCargo(),
                        contato.getEmail(),
                        contato.getTelefone(),
                        contato.getLinkedin(),
                        contato.getTipoContato(),
                        contato.getInteracaoPrincipal(),
                        contato.getCriadoEm()
                );
        }
}