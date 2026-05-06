package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidaturaRepositorio extends JpaRepository<Candidatura, UUID> {

    List<Candidatura> findAllByUsuarioIdAndArquivadaFalse(UUID usuarioId);

    List<Candidatura> findAllByUsuarioIdAndStatus(
            UUID usuarioId, StatusProcessoSeletivoEnum status);

    Optional<Candidatura> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndVagaIdAndArquivadaFalse(UUID usuarioId, UUID vagaId);
}