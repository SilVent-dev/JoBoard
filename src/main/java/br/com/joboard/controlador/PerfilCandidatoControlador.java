package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.PerfilCandidatoRequestDTO;
import br.com.joboard.dominio.DTO.PerfilCandidatoResponseDTO;
import br.com.joboard.servico.PerfilCandidatoServico;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilCandidatoControlador {

    private final PerfilCandidatoServico perfilCandidatoServico;

    public PerfilCandidatoControlador(PerfilCandidatoServico perfilCandidatoServico) {
        this.perfilCandidatoServico = perfilCandidatoServico;
    }

    @GetMapping
    public ResponseEntity<PerfilCandidatoResponseDTO> buscar(){
        return ResponseEntity.ok(perfilCandidatoServico.buscar());
    }

    @PutMapping
    public ResponseEntity<PerfilCandidatoResponseDTO> salvar(@RequestBody @Valid PerfilCandidatoRequestDTO dados){
        return ResponseEntity.ok(perfilCandidatoServico.salvar(dados));
    }
}
