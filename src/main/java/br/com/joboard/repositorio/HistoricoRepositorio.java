package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.HistoricoCandidatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoRepositorio extends JpaRepository<HistoricoCandidatura, UUID> {

    List<HistoricoCandidatura> findAllByCandidaturaIdOrderByDataEventoDesc(UUID candidaturaId);
}