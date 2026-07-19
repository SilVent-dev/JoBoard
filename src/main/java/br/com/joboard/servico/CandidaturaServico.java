package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.*;
import br.com.joboard.dominio.entidade.*;
import br.com.joboard.dominio.enums.*;
import br.com.joboard.dominio.excecao.*;
import br.com.joboard.repositorio.*;
import br.com.joboard.seguranca.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CandidaturaServico {

    private final CandidaturaRepositorio candidaturaRepositorio;
    private final VagaRepositorio vagaRepositorio;
    private final CurriculoRepositorio curriculoRepositorio;
    private final HistoricoServico historicoServico;

    private static final Map<StatusProcessoSeletivoEnum, Set<StatusProcessoSeletivoEnum>> TRANSICOES_VALIDAS;

    static {
        TRANSICOES_VALIDAS = new EnumMap<>(StatusProcessoSeletivoEnum.class);

        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.LISTA_DESEJO, EnumSet.of(
                StatusProcessoSeletivoEnum.APLICADA,
                StatusProcessoSeletivoEnum.DESISTIDA
        ));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.APLICADA, EnumSet.of(
                StatusProcessoSeletivoEnum.TRIAGEM_TELEFONICA,
                StatusProcessoSeletivoEnum.REJEITADA,
                StatusProcessoSeletivoEnum.DESISTIDA
        ));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.TRIAGEM_TELEFONICA, EnumSet.of(
                StatusProcessoSeletivoEnum.ENTREVISTA_TECNICA,
                StatusProcessoSeletivoEnum.REJEITADA,
                StatusProcessoSeletivoEnum.DESISTIDA
        ));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.ENTREVISTA_TECNICA, EnumSet.of(
                StatusProcessoSeletivoEnum.ENTREVISTA_COMPORTAMENTAL,
                StatusProcessoSeletivoEnum.TESTE_PRATICO,
                StatusProcessoSeletivoEnum.REJEITADA,
                StatusProcessoSeletivoEnum.DESISTIDA
        ));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.ENTREVISTA_COMPORTAMENTAL, EnumSet.of(
                StatusProcessoSeletivoEnum.TESTE_PRATICO,
                StatusProcessoSeletivoEnum.PROPOSTA_RECEBIDA,
                StatusProcessoSeletivoEnum.REJEITADA,
                StatusProcessoSeletivoEnum.DESISTIDA
        ));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.TESTE_PRATICO, EnumSet.of(
                StatusProcessoSeletivoEnum.PROPOSTA_RECEBIDA,
                StatusProcessoSeletivoEnum.REJEITADA,
                StatusProcessoSeletivoEnum.DESISTIDA
        ));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.PROPOSTA_RECEBIDA, EnumSet.of(
                StatusProcessoSeletivoEnum.ACEITA,
                StatusProcessoSeletivoEnum.REJEITADA,
                StatusProcessoSeletivoEnum.DESISTIDA
        ));
        // Estados finais — sem transições possíveis
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.ACEITA, EnumSet.noneOf(StatusProcessoSeletivoEnum.class));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.REJEITADA, EnumSet.noneOf(StatusProcessoSeletivoEnum.class));
        TRANSICOES_VALIDAS.put(StatusProcessoSeletivoEnum.DESISTIDA, EnumSet.noneOf(StatusProcessoSeletivoEnum.class));
    }

    private record AcaoSugerida(int diasAteAcao, String descricao) {}

    // Próxima ação sugerida ao entrar em cada status; status finais não têm
    // sugestão (a próxima ação é limpa)
    private static final Map<StatusProcessoSeletivoEnum, AcaoSugerida> ACOES_SUGERIDAS;

    static {
        ACOES_SUGERIDAS = new EnumMap<>(StatusProcessoSeletivoEnum.class);
        ACOES_SUGERIDAS.put(StatusProcessoSeletivoEnum.APLICADA,
                new AcaoSugerida(7, "Fazer follow-up da aplicação"));
        ACOES_SUGERIDAS.put(StatusProcessoSeletivoEnum.TRIAGEM_TELEFONICA,
                new AcaoSugerida(3, "Confirmar próximos passos da triagem"));
        ACOES_SUGERIDAS.put(StatusProcessoSeletivoEnum.ENTREVISTA_TECNICA,
                new AcaoSugerida(5, "Cobrar retorno da entrevista técnica"));
        ACOES_SUGERIDAS.put(StatusProcessoSeletivoEnum.ENTREVISTA_COMPORTAMENTAL,
                new AcaoSugerida(5, "Cobrar retorno da entrevista comportamental"));
        ACOES_SUGERIDAS.put(StatusProcessoSeletivoEnum.TESTE_PRATICO,
                new AcaoSugerida(5, "Cobrar retorno do teste prático"));
        ACOES_SUGERIDAS.put(StatusProcessoSeletivoEnum.PROPOSTA_RECEBIDA,
                new AcaoSugerida(2, "Responder a proposta"));
    }

    @Transactional
    public CandidaturaResponseDTO criar(CandidaturaRequestDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        VagaRastreada vaga = vagaRepositorio
                .findByIdAndUsuarioId(dados.vagaId(), usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga", dados.vagaId()));

        if (!vaga.isVagaAindaAberta()) {
            throw new VagaFechadaException();
        }

        if (candidaturaRepositorio.existsByUsuarioIdAndVagaIdAndArquivadaFalse(
                usuarioLogado.getId(), vaga.getId())) {
            throw new CandidaturaDuplicadaException();
        }

        Curriculo curriculo = null;
        if (dados.curriculoId() != null) {
            curriculo = curriculoRepositorio
                    .findByIdAndUsuarioId(dados.curriculoId(), usuarioLogado.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Curriculo", dados.curriculoId()));
        }

        Candidatura candidatura = Candidatura.builder()
                .usuario(usuarioLogado)
                .vaga(vaga)
                .curriculo(curriculo)
                .plataformaAplicacao(dados.plataformaAplicacao())
                .dataAplicacao(dados.dataAplicacao())
                .cartaApresentacao(dados.cartaApresentacao())
                .portfolioEnviado(dados.portfolioEnviado())
                .proximaAcaoEm(dados.proximaAcaoEm())
                .proximaAcaoDescricao(dados.proximaAcaoDescricao())
                .minhaAvaliacaoInteresse(dados.minhaAvaliacaoInteresse())
                .minhaAvaliacaoFit(dados.minhaAvaliacaoFit())
                .notas(dados.notas())
                .build();

        Candidatura salva = candidaturaRepositorio.save(candidatura);

        historicoServico.registrarMudancaStatus(
                salva, null, StatusProcessoSeletivoEnum.LISTA_DESEJO);

        return CandidaturaResponseDTO.from(salva);
    }

    @Transactional(readOnly = true)
    public List<CandidaturaResponseDTO> listar() {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        return candidaturaRepositorio
                .findAllByUsuarioIdAndArquivadaFalse(usuarioLogado.getId())
                .stream()
                .map(CandidaturaResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CandidaturaResponseDTO buscarPorId(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        return CandidaturaResponseDTO.from(
                candidaturaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", id))
        );
    }

    @Transactional
    public CandidaturaResponseDTO atualizarStatus(UUID id, AtualizarStatusDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        Candidatura candidatura = candidaturaRepositorio
                .findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", id));

        StatusProcessoSeletivoEnum statusAtual = candidatura.getStatus();
        StatusProcessoSeletivoEnum novoStatus = dados.novoStatus();

        Set<StatusProcessoSeletivoEnum> transicoesPossiveis =
                TRANSICOES_VALIDAS.getOrDefault(statusAtual, EnumSet.noneOf(StatusProcessoSeletivoEnum.class));

        if (!transicoesPossiveis.contains(novoStatus)) {
            throw new TransicaoStatusInvalidaException(statusAtual, novoStatus);
        }

        candidatura.setStatus(novoStatus);

        aplicarProximaAcao(candidatura, dados, novoStatus);

        if (novoStatus == StatusProcessoSeletivoEnum.ACEITA) {
            candidatura.setResultadoFinal(ResultadoFinalEnum.APROVADO);
        } else if (novoStatus == StatusProcessoSeletivoEnum.REJEITADA) {
            candidatura.setResultadoFinal(ResultadoFinalEnum.REPROVADO);
        } else if (novoStatus == StatusProcessoSeletivoEnum.DESISTIDA) {
            candidatura.setResultadoFinal(ResultadoFinalEnum.DESISTI);
        }

        Candidatura salva = candidaturaRepositorio.save(candidatura);

        historicoServico.registrarMudancaStatus(salva, statusAtual, novoStatus);

        return CandidaturaResponseDTO.from(salva);
    }

    // Próxima ação explícita do usuário tem prioridade; sem ela, entra a
    // sugestão do mapa. Status final limpa a próxima ação.
    private void aplicarProximaAcao(Candidatura candidatura, AtualizarStatusDTO dados,
                                    StatusProcessoSeletivoEnum novoStatus) {
        if (dados.proximaAcaoEm() != null || dados.proximaAcaoDescricao() != null) {
            candidatura.setProximaAcaoEm(dados.proximaAcaoEm());
            candidatura.setProximaAcaoDescricao(dados.proximaAcaoDescricao());
            return;
        }

        AcaoSugerida sugerida = ACOES_SUGERIDAS.get(novoStatus);
        if (sugerida != null) {
            candidatura.setProximaAcaoEm(LocalDate.now().plusDays(sugerida.diasAteAcao()));
            candidatura.setProximaAcaoDescricao(sugerida.descricao());
        } else {
            candidatura.setProximaAcaoEm(null);
            candidatura.setProximaAcaoDescricao(null);
        }
    }

    @Transactional
    public CandidaturaResponseDTO adicionarNota(UUID id, AdicionarNotaDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        Candidatura candidatura = candidaturaRepositorio
                .findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", id));

        candidatura.setNotas(dados.nota());
        Candidatura salva = candidaturaRepositorio.save(candidatura);

        historicoServico.registrarEvento(
                salva,
                TipoEventoEnum.NOTA_ADICIONADA,
                "Nota adicionada",
                dados.nota()
        );

        return CandidaturaResponseDTO.from(salva);
    }

    @Transactional
    public CandidaturaResponseDTO arquivar(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        Candidatura candidatura = candidaturaRepositorio
                .findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", id));

        if (candidatura.getResultadoFinal() == null) {
            throw new ResultadoFinalNaoDefinidoException();
        }

        candidatura.setArquivada(true);
        candidatura.setArquivadaEm(LocalDateTime.now());

        return CandidaturaResponseDTO.from(candidaturaRepositorio.save(candidatura));
    }

    public List<HistoricoResponseDTO> buscarHistorico(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        candidaturaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", id));

        return historicoServico.buscarPorCandidatura(id);
    }

}