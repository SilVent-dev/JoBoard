package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.VagaRequestDTO;
import br.com.joboard.dominio.DTO.VagaResponseDTO;
import br.com.joboard.dominio.enums.ModeloTrabalhoEnum;
import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoContratoEnum;
import br.com.joboard.servico.VagaServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vagas")
@RequiredArgsConstructor
public class VagaControlador {

    private final VagaServico vagaServico;

    @PostMapping
    public ResponseEntity<VagaResponseDTO> criar(@RequestBody @Valid VagaRequestDTO dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vagaServico.criar(dados));
    }

    @GetMapping
    public ResponseEntity<List<VagaResponseDTO>> listar(
            @RequestParam(required = false) ModeloTrabalhoEnum modeloTrabalho,
            @RequestParam(required = false) TipoContratoEnum tipoContrato,
            @RequestParam(required = false) NivelExperienciaEnum nivelExperiencia,
            @RequestParam(required = false) Boolean vagaAindaAberta) {
        return ResponseEntity.ok(
                vagaServico.listar(modeloTrabalho, tipoContrato, nivelExperiencia, vagaAindaAberta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VagaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(vagaServico.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VagaResponseDTO> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid VagaRequestDTO dados) {
        return ResponseEntity.ok(vagaServico.atualizar(id, dados));
    }

    @PatchMapping("/{id}/fechar")
    public ResponseEntity<VagaResponseDTO> fecharVaga(@PathVariable UUID id) {
        return ResponseEntity.ok(vagaServico.fecharVaga(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        vagaServico.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
