package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.CurriculoResponseDTO;
import br.com.joboard.servico.CurriculoServico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/curriculos")
@RequiredArgsConstructor
public class CurriculoControlador {

    private final CurriculoServico curriculoServico;

    @PostMapping
    public ResponseEntity<CurriculoResponseDTO> upload(
            @RequestParam MultipartFile arquivo,
            @RequestParam @NotBlank @Size(max = 100) String versao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(curriculoServico.upload(arquivo, versao));
    }

    @GetMapping
    public ResponseEntity<List<CurriculoResponseDTO>> listar() {
        return ResponseEntity.ok(curriculoServico.listar());
    }

    @PatchMapping("/{id}/principal")
    public ResponseEntity<CurriculoResponseDTO> marcarComoPrincipal(@PathVariable UUID id) {
        return ResponseEntity.ok(curriculoServico.marcarComoPrincipal(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        curriculoServico.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
