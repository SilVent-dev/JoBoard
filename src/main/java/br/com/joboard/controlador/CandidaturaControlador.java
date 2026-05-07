package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.*;
import br.com.joboard.servico.CandidaturaServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidaturas")
@RequiredArgsConstructor
public class CandidaturaControlador {

    private final CandidaturaServico candidaturaServico;

    @PostMapping
    public ResponseEntity<CandidaturaResponseDTO> criar(
            @RequestBody @Valid CandidaturaRequestDTO dados) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidaturaServico.criar(dados));
    }

    @GetMapping
    public ResponseEntity<List<CandidaturaResponseDTO>> listar() {
        return ResponseEntity.ok(candidaturaServico.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidaturaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(candidaturaServico.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidaturaResponseDTO> atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarStatusDTO dados) {
        return ResponseEntity.ok(candidaturaServico.atualizarStatus(id, dados));
    }

    @PatchMapping("/{id}/nota")
    public ResponseEntity<CandidaturaResponseDTO> adicionarNota(
            @PathVariable UUID id,
            @RequestBody @Valid AdicionarNotaDTO dados) {
        return ResponseEntity.ok(candidaturaServico.adicionarNota(id, dados));
    }

    @PatchMapping("/{id}/arquivar")
    public ResponseEntity<CandidaturaResponseDTO> arquivar(@PathVariable UUID id) {
        return ResponseEntity.ok(candidaturaServico.arquivar(id));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoResponseDTO>> buscarHistorico(
            @PathVariable UUID id) {
        return ResponseEntity.ok(candidaturaServico.buscarHistorico(id));
    }
}