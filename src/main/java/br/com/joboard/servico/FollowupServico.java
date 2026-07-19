package br.com.joboard.servico;

import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.PerfilCandidatoRepositorio;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job diário de follow-up: um único email por usuário por dia, listando as
 * candidaturas com próxima ação para hoje ou vencida. Respeita o opt-out
 * do perfil (aceitaEmailFollowup).
 */
@Service
@RequiredArgsConstructor
public class FollowupServico {

    private static final Logger log = LoggerFactory.getLogger(FollowupServico.class);

    private static final Set<StatusProcessoSeletivoEnum> STATUS_FINAIS = EnumSet.of(
            StatusProcessoSeletivoEnum.ACEITA,
            StatusProcessoSeletivoEnum.REJEITADA,
            StatusProcessoSeletivoEnum.DESISTIDA
    );

    private final CandidaturaRepositorio candidaturaRepositorio;
    private final PerfilCandidatoRepositorio perfilCandidatoRepositorio;
    private final EmailServico emailServico;

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Sao_Paulo")
    @Transactional(readOnly = true)
    public void enviarLembretesDiarios() {
        Map<Usuario, List<Candidatura>> porUsuario = candidaturaRepositorio
                .buscarComFollowupPendente(LocalDate.now(), STATUS_FINAIS)
                .stream()
                .collect(Collectors.groupingBy(Candidatura::getUsuario));

        porUsuario.forEach((usuario, candidaturas) -> {
            boolean aceita = perfilCandidatoRepositorio.findByUsuarioId(usuario.getId())
                    .map(perfil -> !Boolean.FALSE.equals(perfil.getAceitaEmailFollowup()))
                    .orElse(true);
            if (!aceita) {
                return;
            }

            try {
                emailServico.enviarFollowupDiario(
                        usuario.getEmail(), usuario.getNome(), montarItens(candidaturas));
            } catch (Exception e) {
                // Falha em um destinatário não pode derrubar o lote inteiro
                log.warn("Falha ao enviar follow-up diário para {}", usuario.getEmail(), e);
            }
        });
    }

    private String montarItens(List<Candidatura> candidaturas) {
        return candidaturas.stream()
                .map(c -> "<li style=\"margin-bottom:8px;\"><strong>"
                        + c.getVaga().getTitulo() + "</strong> — "
                        + c.getVaga().getEmpresa().getNome() + ": "
                        + c.getProximaAcaoDescricao() + "</li>")
                .collect(Collectors.joining());
    }
}
