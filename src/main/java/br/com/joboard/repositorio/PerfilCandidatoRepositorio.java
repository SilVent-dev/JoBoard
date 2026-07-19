package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.PerfilCandidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PerfilCandidatoRepositorio extends JpaRepository<PerfilCandidato, UUID> {

    Optional<PerfilCandidato> findByUsuarioId(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM PerfilCandidato p WHERE p.usuario.id = :usuarioId")
    void deletarDoUsuario(UUID usuarioId);
}
