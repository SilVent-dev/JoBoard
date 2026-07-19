package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContatoRepositorio extends JpaRepository<Contato, UUID> {

    List<Contato> findAllByCandidaturaId(UUID candidaturaId);

    Optional<Contato> findByIdAndCandidaturaId(UUID id, UUID candidaturaId);

    List<Contato> findAllByCandidaturaUsuarioId(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM Contato c WHERE c.candidatura.usuario.id = :usuarioId")
    void deletarTodosDoUsuario(UUID usuarioId);
}