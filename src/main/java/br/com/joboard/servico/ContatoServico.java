package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.ContatoRequestDTO;
import br.com.joboard.dominio.DTO.ContatoResponseDTO;
import br.com.joboard.dominio.entidade.Candidatura;
import br.com.joboard.dominio.entidade.Contato;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.ContatoRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContatoServico {

    private final ContatoRepositorio contatoRepositorio;
    private final CandidaturaRepositorio candidaturaRepositorio;

    @Transactional
    public ContatoResponseDTO criar(UUID candidaturaId, ContatoRequestDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        Candidatura candidatura = candidaturaRepositorio
                .findByIdAndUsuarioId(candidaturaId, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", candidaturaId));

        Contato contato = Contato.builder()
                .candidatura(candidatura)
                .nome(dados.nome())
                .cargo(dados.cargo())
                .email(dados.email())
                .telefone(dados.telefone())
                .linkedin(dados.linkedin())
                .tipoContato(dados.tipoContato())
                .interacaoPrincipal(dados.interacaoPrincipal())
                .build();

        return ContatoResponseDTO.from(contatoRepositorio.save(contato));
    }

    public List<ContatoResponseDTO> listar(UUID candidaturaId) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        candidaturaRepositorio.findByIdAndUsuarioId(candidaturaId, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", candidaturaId));

        return contatoRepositorio.findAllByCandidaturaId(candidaturaId)
                .stream()
                .map(ContatoResponseDTO::from)
                .toList();
    }

    @Transactional
    public ContatoResponseDTO atualizar(UUID candidaturaId, UUID contatoId,
                                        ContatoRequestDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        candidaturaRepositorio.findByIdAndUsuarioId(candidaturaId, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", candidaturaId));

        Contato contato = contatoRepositorio
                .findByIdAndCandidaturaId(contatoId, candidaturaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contato", contatoId));

        contato.setNome(dados.nome());
        contato.setCargo(dados.cargo());
        contato.setEmail(dados.email());
        contato.setTelefone(dados.telefone());
        contato.setLinkedin(dados.linkedin());
        contato.setTipoContato(dados.tipoContato());
        contato.setInteracaoPrincipal(dados.interacaoPrincipal());

        return ContatoResponseDTO.from(contatoRepositorio.save(contato));
    }

    @Transactional
    public void deletar(UUID candidaturaId, UUID contatoId) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        candidaturaRepositorio.findByIdAndUsuarioId(candidaturaId, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura", candidaturaId));

        Contato contato = contatoRepositorio
                .findByIdAndCandidaturaId(contatoId, candidaturaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contato", contatoId));

        contatoRepositorio.delete(contato);
    }
}