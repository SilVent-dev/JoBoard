package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.VagaRastreada;
import br.com.joboard.dominio.enums.ModeloTrabalhoEnum;
import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoContratoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VagaRepositorio extends JpaRepository<VagaRastreada, UUID> {

    Optional<VagaRastreada> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    @Query("""
        SELECT v FROM VagaRastreada v
        WHERE v.usuario.id = :usuarioId
        AND (:modeloTrabalho IS NULL OR v.modeloTrabalho = :modeloTrabalho)
        AND (:tipoContrato   IS NULL OR v.tipoContrato   = :tipoContrato)
        AND (:nivelExperiencia IS NULL OR v.nivelExperiencia = :nivelExperiencia)
        AND (:vagaAindaAberta IS NULL OR v.vagaAindaAberta = :vagaAindaAberta)
        ORDER BY v.criadoEm DESC
    """)
    List<VagaRastreada> buscarComFiltros(
            @Param("usuarioId") UUID usuarioId,
            @Param("modeloTrabalho") ModeloTrabalhoEnum modeloTrabalho,
            @Param("tipoContrato") TipoContratoEnum tipoContrato,
            @Param("nivelExperiencia") NivelExperienciaEnum nivelExperiencia,
            @Param("vagaAindaAberta") Boolean vagaAindaAberta
    );
}
