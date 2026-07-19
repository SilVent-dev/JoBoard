package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.Curriculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurriculoRepositorio extends JpaRepository<Curriculo, UUID> {

    List<Curriculo> findAllByUsuarioId(UUID usuarioId);

    Optional<Curriculo> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndVersao(UUID usuarioId, String versao);

    @Modifying
    @Query("UPDATE Curriculo c SET c.ehPrincipal = false WHERE c.usuario.id = :usuarioId")
    void desmarcarTodosPrincipais(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM Curriculo c WHERE c.usuario.id = :usuarioId")
    void deletarTodosDoUsuario(UUID usuarioId);
}
