package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.PerfilCandidato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilCandidatoRepositorio extends JpaRepository<PerfilCandidato, UUID> {

    Optional<PerfilCandidato> findByUsuarioId(UUID usuarioId);
    boolean existsByCpfAndUsuarioIdNot(String cpf, UUID usuarioId);
}
