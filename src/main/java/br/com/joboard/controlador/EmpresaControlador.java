package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.EmpresaRequestDTO;
import br.com.joboard.dominio.DTO.EmpresaResponseDTO;
import br.com.joboard.servico.EmpresaServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas")
public class EmpresaControlador {

    private final EmpresaServico empresaServico;

    @PostMapping
    public ResponseEntity<EmpresaResponseDTO> criar(
            @RequestBody @Valid EmpresaRequestDTO dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaServico.criar(dados));
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponseDTO>> listar() {
        return ResponseEntity.ok(empresaServico.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(empresaServico.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid EmpresaRequestDTO dados) {
        return ResponseEntity.ok(empresaServico.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        empresaServico.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
