package br.com.joboard.servico;

import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.entidade.EmpresaCatalogada;
import br.com.joboard.dominio.entidade.PerfilCandidato;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.entidade.VagaRastreada;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.PerfilCandidatoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Job diário de follow-up: um email por usuário, opt-out respeitado,
 * e falha em um destinatário não derruba o lote.
 */
@ExtendWith(MockitoExtension.class)
class FollowupServicoTest {

    @Mock
    private CandidaturaRepositorio candidaturaRepositorio;
    @Mock
    private PerfilCandidatoRepositorio perfilCandidatoRepositorio;
    @Mock
    private EmailServico emailServico;

    private FollowupServico followupServico;

    @BeforeEach
    void setUp() {
        followupServico = new FollowupServico(
                candidaturaRepositorio, perfilCandidatoRepositorio, emailServico);
    }

    private Usuario usuario(String email) {
        return Usuario.builder().id(UUID.randomUUID()).email(email).nome("Pessoa").build();
    }

    private Candidatura candidaturaPendente(Usuario usuario, String vagaTitulo) {
        VagaRastreada vaga = VagaRastreada.builder()
                .titulo(vagaTitulo)
                .empresa(EmpresaCatalogada.builder().nome("Acme").build())
                .build();
        return Candidatura.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .vaga(vaga)
                .proximaAcaoEm(LocalDate.now())
                .proximaAcaoDescricao("Fazer follow-up")
                .build();
    }

    @Test
    void enviaUmEmailPorUsuarioComSuasCandidaturas() {
        Usuario ana = usuario("ana@teste.com");
        when(candidaturaRepositorio.buscarComFollowupPendente(any(), any()))
                .thenReturn(List.of(
                        candidaturaPendente(ana, "Dev Backend"),
                        candidaturaPendente(ana, "Dev Frontend")));

        followupServico.enviarLembretesDiarios();

        verify(emailServico).enviarFollowupDiario(
                eq("ana@teste.com"), eq("Pessoa"), contains("Dev Backend"));
    }

    @Test
    void respeitaOptOutDoPerfil() {
        Usuario ana = usuario("ana@teste.com");
        when(candidaturaRepositorio.buscarComFollowupPendente(any(), any()))
                .thenReturn(List.of(candidaturaPendente(ana, "Dev Backend")));
        when(perfilCandidatoRepositorio.findByUsuarioId(ana.getId()))
                .thenReturn(Optional.of(PerfilCandidato.builder()
                        .aceitaEmailFollowup(false)
                        .build()));

        followupServico.enviarLembretesDiarios();

        verify(emailServico, never()).enviarFollowupDiario(any(), any(), any());
    }

    @Test
    void falhaEmUmDestinatarioNaoDerrubaOLote() {
        Usuario ana = usuario("ana@teste.com");
        Usuario bia = usuario("bia@teste.com");
        when(candidaturaRepositorio.buscarComFollowupPendente(any(), any()))
                .thenReturn(List.of(
                        candidaturaPendente(ana, "Dev Backend"),
                        candidaturaPendente(bia, "Dev Frontend")));
        doThrow(new RuntimeException("smtp fora"))
                .when(emailServico).enviarFollowupDiario(eq("ana@teste.com"), any(), any());

        followupServico.enviarLembretesDiarios();

        verify(emailServico).enviarFollowupDiario(eq("bia@teste.com"), any(), any());
    }
}
