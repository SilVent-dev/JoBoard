package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.TipoUsuario;
import br.com.joboard.dominio.enums.TipoUsuarioEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoUsuarioRepositorio extends JpaRepository<TipoUsuario, UUID> {

    Optional<TipoUsuario> findByNome(TipoUsuarioEnum nome);
}
