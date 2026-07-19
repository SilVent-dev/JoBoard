package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.InsightsResponseDTO;
import br.com.joboard.servico.InsightServico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightControlador {

    private final InsightServico insightServico;

    public InsightControlador(InsightServico insightServico) {
        this.insightServico = insightServico;
    }

    @GetMapping
    public ResponseEntity<InsightsResponseDTO> gerar() {
        return ResponseEntity.ok(insightServico.gerar());
    }
}
