package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidaturaRepositorio extends JpaRepository<Candidatura, UUID> {

    List<Candidatura> findAllByUsuarioIdAndArquivadaFalse(UUID usuarioId);

    List<Candidatura> findAllByUsuarioId(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM Candidatura c WHERE c.usuario.id = :usuarioId")
    void deletarTodosDoUsuario(UUID usuarioId);

    List<Candidatura> findAllByUsuarioIdAndStatus(
            UUID usuarioId, StatusProcessoSeletivoEnum status);

    Optional<Candidatura> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    @Query("""
            SELECT c FROM Candidatura c
            WHERE c.arquivada = false
                AND c.proximaAcaoEm <= :ateData
                AND c.status NOT IN :statusFinais
            """)
    List<Candidatura> buscarComFollowupPendente(
            java.time.LocalDate ateData, java.util.Collection<StatusProcessoSeletivoEnum> statusFinais);

    boolean existsByUsuarioIdAndVagaIdAndArquivadaFalse(UUID usuarioId, UUID vagaId);
}