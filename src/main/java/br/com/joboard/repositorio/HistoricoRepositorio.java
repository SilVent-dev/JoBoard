package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.HistoricoCandidatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface HistoricoRepositorio extends JpaRepository<HistoricoCandidatura, UUID> {

    List<HistoricoCandidatura> findAllByCandidaturaIdOrderByDataEventoDesc(UUID candidaturaId);

    List<HistoricoCandidatura> findAllByCandidaturaUsuarioId(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM HistoricoCandidatura h WHERE h.candidatura.usuario.id = :usuarioId")
    void deletarTodosDoUsuario(UUID usuarioId);
}