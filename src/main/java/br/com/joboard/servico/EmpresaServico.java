package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.EmpresaRequestDTO;
import br.com.joboard.dominio.DTO.EmpresaResponseDTO;
import br.com.joboard.dominio.entidade.EmpresaCatalogada;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.EmpresaJaCadastradaException;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.EmpresaRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaServico {

    private final EmpresaRepositorio empresaRepositorio;

    @Transactional
    public EmpresaResponseDTO criar(EmpresaRequestDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        if (empresaRepositorio.existsByUsuarioIdAndNome(usuarioLogado.getId(), dados.nome())) {
            throw new EmpresaJaCadastradaException(dados.nome());
        }

        EmpresaCatalogada empresa = EmpresaCatalogada.builder()
                .usuario(usuarioLogado)
                .nome(dados.nome())
                .site(dados.site())
                .localizacao(dados.localizacao())
                .porte(dados.porte())
                .setor(dados.setor())
                .culturaObservacoes(dados.culturaObservacoes())
                .contatoRh(dados.contatoRh())
                .contatoReferencia(dados.contatoReferencia())
                .build();

        return EmpresaResponseDTO.from(empresaRepositorio.save(empresa));
    }

    public List<EmpresaResponseDTO> listar() {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        return empresaRepositorio.findAllByUsuarioId(usuarioLogado.getId())
                .stream()
                .map(EmpresaResponseDTO::from)
                .toList();
    }

    public EmpresaResponseDTO buscarPorId(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        EmpresaCatalogada empresa = empresaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));
        return EmpresaResponseDTO.from(empresa);
    }

    @Transactional
    public EmpresaResponseDTO atualizar(UUID id, EmpresaRequestDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        EmpresaCatalogada empresa = empresaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));

        if (!empresa.getNome().equalsIgnoreCase(dados.nome()) &&
                empresaRepositorio.existsByUsuarioIdAndNome(usuarioLogado.getId(), dados.nome())) {
            throw new EmpresaJaCadastradaException(dados.nome());
        }

        empresa.setNome(dados.nome());
        empresa.setSite(dados.site());
        empresa.setLocalizacao(dados.localizacao());
        empresa.setPorte(dados.porte());
        empresa.setSetor(dados.setor());
        empresa.setCulturaObservacoes(dados.culturaObservacoes());
        empresa.setContatoRh(dados.contatoRh());
        empresa.setContatoReferencia(dados.contatoReferencia());

        return EmpresaResponseDTO.from(empresaRepositorio.save(empresa));
    }

    @Transactional
    public void deletar(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        EmpresaCatalogada empresa = empresaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));
        empresaRepositorio.delete(empresa);
    }
}
