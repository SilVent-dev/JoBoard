package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.entidade.EmpresaCatalogada;
import br.com.joboard.dominio.enums.PorteEmpresaEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmpresaResponseDTO(UUID id,
                                 String nome,
                                 String site,
                                 String localizacao,
                                 PorteEmpresaEnum porte,
                                 String setor,
                                 String culturaObservacoes,
                                 String contatoRh,
                                 String contatoReferencia,
                                 LocalDateTime criadoEm,
                                 LocalDateTime atualizadoEm) {
    public static EmpresaResponseDTO from(EmpresaCatalogada empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNome(),
                empresa.getSite(),
                empresa.getLocalizacao(),
                empresa.getPorte(),
                empresa.getSetor(),
                empresa.getCulturaObservacoes(),
                empresa.getContatoRh(),
                empresa.getContatoReferencia(),
                empresa.getCriadoEm(),
                empresa.getAtualizadoEm()
        );
    }
}
