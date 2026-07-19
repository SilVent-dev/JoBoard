package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.EmpresaCatalogada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepositorio extends JpaRepository<EmpresaCatalogada, UUID> {

    List<EmpresaCatalogada> findAllByUsuarioId(UUID usuarioId);

    Optional<EmpresaCatalogada> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndNome(UUID usuarioId, String nome);

    @Modifying
    @Query("DELETE FROM EmpresaCatalogada e WHERE e.usuario.id = :usuarioId")
    void deletarTodosDoUsuario(UUID usuarioId);
}
