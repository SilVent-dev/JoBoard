package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.TokenRedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TokenRedefinicaoSenhaRepositorio extends JpaRepository<TokenRedefinicaoSenha, UUID> {

    Optional<TokenRedefinicaoSenha> findByToken(String token);

    @Query("""
            FROM TokenRedefinicaoSenha t
            WHERE t.usuario.id = :usuarioId
                AND t.usadoEm IS NULL
            """)
    Optional<TokenRedefinicaoSenha> encontrarPendentePorUsuario(UUID usuarioId);

    long countByUsuarioIdAndCriadoEmAfter(UUID usuarioId, LocalDateTime desde);

    @Modifying
    @Query("DELETE FROM TokenRedefinicaoSenha t WHERE t.usuario.id = :usuarioId")
    void deletarTodosDoUsuario(UUID usuarioId);
}
