package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.TokenVerificacaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TokenVerificacaoEmailRepositorio extends JpaRepository<TokenVerificacaoEmail, UUID> {

    Optional<TokenVerificacaoEmail> findByToken(String token);

    @Query("""
            FROM TokenVerificacaoEmail t 
            WHERE t.usuario.id = :usuarioId
                AND t.usadoEm IS NULL
            """)
    Optional<TokenVerificacaoEmail> encontrarPendentePorUsuario (UUID usuarioId);

}
