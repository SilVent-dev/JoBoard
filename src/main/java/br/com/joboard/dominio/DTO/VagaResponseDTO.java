package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.entidade.VagaRastreada;
import br.com.joboard.dominio.enums.ModeloTrabalhoEnum;
import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoContratoEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record VagaResponseDTO(UUID id,
                              UUID empresaId,
                              String empresaNome,
                              String titulo,
                              String descricao,
                              String urlVaga,
                              String localizacao,
                              ModeloTrabalhoEnum modeloTrabalho,
                              TipoContratoEnum tipoContrato,
                              NivelExperienciaEnum nivelExperiencia,
                              String faixaSalarial,
                              String beneficios,
                              String requisitosObrigatorios,
                              String requisitosDesejaveis,
                              boolean vagaAindaAberta,
                              LocalDateTime criadoEm,
                              LocalDateTime atualizadoEm) {
    public static VagaResponseDTO from(VagaRastreada vaga) {
        return new VagaResponseDTO(
                vaga.getId(),
                vaga.getEmpresa().getId(),
                vaga.getEmpresa().getNome(),
                vaga.getTitulo(),
                vaga.getDescricao(),
                vaga.getUrlVaga(),
                vaga.getLocalizacao(),
                vaga.getModeloTrabalho(),
                vaga.getTipoContrato(),
                vaga.getNivelExperiencia(),
                vaga.getFaixaSalarial(),
                vaga.getBeneficios(),
                vaga.getRequisitosObrigatorios(),
                vaga.getRequisitosDesejaveis(),
                vaga.isVagaAindaAberta(),
                vaga.getCriadoEm(),
                vaga.getAtualizadoEm()
        );
    }
}
