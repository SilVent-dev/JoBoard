package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.entidade.PerfilCandidato;
import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoDisponibilidadeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PerfilCandidatoResponseDTO(
        UUID id,
        UUID usuarioId,
        String nomeCompleto,
        String telefone,
        String cidade,
        String estado,
        Boolean aceitaRemoto,
        Boolean aceitaHibrido,
        Boolean aceitaPresencial,
        NivelExperienciaEnum nivelExperiencia,
        TipoDisponibilidadeEnum disponibilidade,
        BigDecimal pretensaoSalarialMin,
        BigDecimal pretensaoSalarialMax,
        String urlLinkedin,
        String urlGithub,
        String urlPortfolio,
        String resumoProfissional,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm

) {
        public static PerfilCandidatoResponseDTO from (PerfilCandidato perfil){
                return new PerfilCandidatoResponseDTO(
                        perfil.getId(),
                        perfil.getUsuario().getId(),
                        perfil.getNomeCompleto(),
                        perfil.getTelefone(),
                        perfil.getCidade(),
                        perfil.getEstado(), 
                        perfil.getAceitaRemoto(),
                        perfil.getAceitaHibrido(),
                        perfil.getAceitaPresencial(),
                        perfil.getNivelExperiencia(),
                        perfil.getDisponibilidade(),
                        perfil.getPretensaoSalarialMin(),
                        perfil.getPretensaoSalarialMax(),
                        perfil.getUrlLinkedin(),
                        perfil.getUrlGithub(),
                        perfil.getUrlPortfolio(),
                        perfil.getResumoProfissional(),
                        perfil.getCriadoEm(),
                        perfil.getAtualizadoEm()
                );
        }
}
