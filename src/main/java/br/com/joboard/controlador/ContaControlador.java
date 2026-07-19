package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.ExcluirContaDTO;
import br.com.joboard.servico.ContaServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conta")
public class ContaControlador {

    private final ContaServico contaServico;

    public ContaControlador(ContaServico contaServico) {
        this.contaServico = contaServico;
    }

    @DeleteMapping
    public ResponseEntity<Void> excluir(@RequestBody @Valid ExcluirContaDTO dados) {
        contaServico.excluirConta(dados.senha());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exportacao")
    public ResponseEntity<byte[]> exportar() {
        byte[] zip = contaServico.exportarDados();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"joboard-dados.zip\"")
                .contentType(MediaType.valueOf("application/zip"))
                .body(zip);
    }
}
