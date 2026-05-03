package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.VagaRequestDTO;
import br.com.joboard.dominio.DTO.VagaResponseDTO;
import br.com.joboard.dominio.entidade.EmpresaCatalogada;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.entidade.VagaRastreada;
import br.com.joboard.dominio.enums.ModeloTrabalhoEnum;
import br.com.joboard.dominio.enums.NivelExperienciaEnum;
import br.com.joboard.dominio.enums.TipoContratoEnum;
import br.com.joboard.dominio.excecao.AcessoNegadoException;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.EmpresaRepositorio;
import br.com.joboard.repositorio.VagaRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VagaServico {

    private final VagaRepositorio vagaRepositorio;
    private final EmpresaRepositorio empresaRepositorio;

    @Transactional
    public VagaResponseDTO criar(VagaRequestDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        EmpresaCatalogada empresa = empresaRepositorio
                .findByIdAndUsuarioId(dados.empresaId(), usuarioLogado.getId())
                .orElseThrow(AcessoNegadoException::new);

        VagaRastreada vaga = VagaRastreada.builder()
                .usuario(usuarioLogado)
                .empresa(empresa)
                .titulo(dados.titulo())
                .descricao(dados.descricao())
                .urlVaga(dados.urlVaga())
                .localizacao(dados.localizacao())
                .modeloTrabalho(dados.modeloTrabalho())
                .tipoContrato(dados.tipoContrato())
                .nivelExperiencia(dados.nivelExperiencia())
                .faixaSalarial(dados.faixaSalarial())
                .beneficios(dados.beneficios())
                .requisitosObrigatorios(dados.requisitosObrigatorios())
                .requisitosDesejaveis(dados.requisitosDesejaveis())
                .build();

        return VagaResponseDTO.from(vagaRepositorio.save(vaga));
    }

    public List<VagaResponseDTO> listar(
            ModeloTrabalhoEnum modeloTrabalho,
            TipoContratoEnum tipoContrato,
            NivelExperienciaEnum nivelExperiencia,
            Boolean vagaAindaAberta) {

        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        return vagaRepositorio.buscarComFiltros(
                        usuarioLogado.getId(),
                        modeloTrabalho,
                        tipoContrato,
                        nivelExperiencia,
                        vagaAindaAberta)
                .stream()
                .map(VagaResponseDTO::from)
                .toList();
    }

    public VagaResponseDTO buscarPorId(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        return VagaResponseDTO.from(
                vagaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga", id))
        );
    }

    @Transactional
    public VagaResponseDTO atualizar(UUID id, VagaRequestDTO dados) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        VagaRastreada vaga = vagaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga", id));

        EmpresaCatalogada empresa = empresaRepositorio
                .findByIdAndUsuarioId(dados.empresaId(), usuarioLogado.getId())
                .orElseThrow(() -> new AcessoNegadoException());


        vaga.setEmpresa(empresa);
        vaga.setTitulo(dados.titulo());
        vaga.setDescricao(dados.descricao());
        vaga.setUrlVaga(dados.urlVaga());
        vaga.setLocalizacao(dados.localizacao());
        vaga.setModeloTrabalho(dados.modeloTrabalho());
        vaga.setTipoContrato(dados.tipoContrato());
        vaga.setNivelExperiencia(dados.nivelExperiencia());
        vaga.setFaixaSalarial(dados.faixaSalarial());
        vaga.setBeneficios(dados.beneficios());
        vaga.setRequisitosObrigatorios(dados.requisitosObrigatorios());
        vaga.setRequisitosDesejaveis(dados.requisitosDesejaveis());

        return VagaResponseDTO.from(vagaRepositorio.save(vaga));
    }

    @Transactional
    public VagaResponseDTO fecharVaga(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        VagaRastreada vaga = vagaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga", id));

        vaga.setVagaAindaAberta(false);
        return VagaResponseDTO.from(vagaRepositorio.save(vaga));
    }

    @Transactional
    public void deletar(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        VagaRastreada vaga = vagaRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga", id));
        vagaRepositorio.delete(vaga);
    }
}
