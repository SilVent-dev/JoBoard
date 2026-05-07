package br.com.joboard.repositorio;

import br.com.joboard.dominio.entidade.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContatoRepositorio extends JpaRepository<Contato, UUID> {

    List<Contato> findAllByCandidaturaId(UUID candidaturaId);

    Optional<Contato> findByIdAndCandidaturaId(UUID id, UUID candidaturaId);
}