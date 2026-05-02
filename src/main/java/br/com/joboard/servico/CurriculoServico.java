package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.CurriculoResponseDTO;
import br.com.joboard.dominio.entidade.Curriculo;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.CurriculoRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurriculoServico {

    private final CurriculoRepositorio curriculoRepositorio;
    private final StorageServico storageServico;

    @Transactional
    public CurriculoResponseDTO upload(MultipartFile arquivo, String versao) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        if (curriculoRepositorio.existsByUsuarioIdAndVersao(usuarioLogado.getId(), versao)) {
            throw new IllegalArgumentException("Já existe um currículo com essa versão.");
        }

        String urlArquivo = storageServico.upload(arquivo, usuarioLogado.getId());

        boolean ehPrimeiro = curriculoRepositorio.findAllByUsuarioId(usuarioLogado.getId()).isEmpty();

        Curriculo curriculo = Curriculo.builder()
                .usuario(usuarioLogado)
                .nomeArquivo(arquivo.getOriginalFilename())
                .urlArquivo(urlArquivo)
                .tamanhoBytes(arquivo.getSize())
                .tipoMime(arquivo.getContentType())
                .versao(versao)
                .ehPrincipal(ehPrimeiro) // primeiro currículo já vira principal automaticamente
                .build();

        return CurriculoResponseDTO.from(curriculoRepositorio.save(curriculo));
    }

    public List<CurriculoResponseDTO> listar() {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        return curriculoRepositorio.findAllByUsuarioId(usuarioLogado.getId())
                .stream()
                .map(CurriculoResponseDTO::from)
                .toList();
    }

    @Transactional
    public CurriculoResponseDTO marcarComoPrincipal(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        Curriculo curriculo = curriculoRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curriculo", id));

        // desmarca todos do usuário e marca só esse —> operação atômica
        curriculoRepositorio.desmarcarTodosPrincipais(usuarioLogado.getId());
        curriculo.setEhPrincipal(true);

        return CurriculoResponseDTO.from(curriculoRepositorio.save(curriculo));
    }

    @Transactional
    public void deletar(UUID id) {
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        Curriculo curriculo = curriculoRepositorio.findByIdAndUsuarioId(id, usuarioLogado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curriculo", id));

        // storage primeiro —> se falhar, banco não é tocado
        storageServico.deletar(curriculo.getUrlArquivo());
        curriculoRepositorio.delete(curriculo);
    }
}
