package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.HistoricoResponseDTO;
import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.entidade.HistoricoCandidatura;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import br.com.joboard.dominio.enums.TipoEventoEnum;
import br.com.joboard.repositorio.HistoricoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoricoServico {

    private final HistoricoRepositorio historicoRepositorio;

    public void registrarMudancaStatus(
            Candidatura candidatura,
            StatusProcessoSeletivoEnum statusAnterior,
            StatusProcessoSeletivoEnum statusNovo) {

        HistoricoCandidatura evento = HistoricoCandidatura.builder()
                .candidatura(candidatura)
                .tipoEvento(TipoEventoEnum.MUDANCA_STATUS)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .tituloEvento("Status alterado para: " + statusNovo.getDescricao())
                .dataEvento(LocalDate.now())
                .build();

        historicoRepositorio.save(evento);
    }

    public void registrarEvento(
            Candidatura candidatura,
            TipoEventoEnum tipoEvento,
            String titulo,
            String descricao) {

        HistoricoCandidatura evento = HistoricoCandidatura.builder()
                .candidatura(candidatura)
                .tipoEvento(tipoEvento)
                .tituloEvento(titulo)
                .descricao(descricao)
                .dataEvento(LocalDate.now())
                .build();

        historicoRepositorio.save(evento);
    }

    public List<HistoricoResponseDTO> buscarPorCandidatura(UUID candidaturaId) {
        return historicoRepositorio
                .findAllByCandidaturaIdOrderByDataEventoDesc(candidaturaId)
                .stream()
                .map(HistoricoResponseDTO::from)
                .toList();
    }
}