package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.ContatoRequestDTO;
import br.com.joboard.dominio.DTO.ContatoResponseDTO;
import br.com.joboard.servico.ContatoServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidaturas/{candidaturaId}/contatos")
@RequiredArgsConstructor
public class ContatoControlador {

    private final ContatoServico contatoServico;

    @PostMapping
    public ResponseEntity<ContatoResponseDTO> criar(
            @PathVariable UUID candidaturaId,
            @RequestBody @Valid ContatoRequestDTO dados) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contatoServico.criar(candidaturaId, dados));
    }

    @GetMapping
    public ResponseEntity<List<ContatoResponseDTO>> listar(
            @PathVariable UUID candidaturaId) {
        return ResponseEntity.ok(contatoServico.listar(candidaturaId));
    }

    @PutMapping("/{contatoId}")
    public ResponseEntity<ContatoResponseDTO> atualizar(
            @PathVariable UUID candidaturaId,
            @PathVariable UUID contatoId,
            @RequestBody @Valid ContatoRequestDTO dados) {
        return ResponseEntity.ok(contatoServico.atualizar(candidaturaId, contatoId, dados));
    }

    @DeleteMapping("/{contatoId}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID candidaturaId,
            @PathVariable UUID contatoId) {
        contatoServico.deletar(candidaturaId, contatoId);
        return ResponseEntity.noContent().build();
    }
}