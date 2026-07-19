package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.CandidaturaResponseDTO;
import br.com.joboard.dominio.DTO.ContatoResponseDTO;
import br.com.joboard.dominio.DTO.CurriculoResponseDTO;
import br.com.joboard.dominio.DTO.EmpresaResponseDTO;
import br.com.joboard.dominio.DTO.PerfilCandidatoResponseDTO;
import br.com.joboard.dominio.DTO.VagaResponseDTO;
import br.com.joboard.dominio.entidade.Curriculo;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.SenhaIncorretaException;
import br.com.joboard.repositorio.CandidaturaRepositorio;
import br.com.joboard.repositorio.ContatoRepositorio;
import br.com.joboard.repositorio.CurriculoRepositorio;
import br.com.joboard.repositorio.EmpresaRepositorio;
import br.com.joboard.repositorio.HistoricoRepositorio;
import br.com.joboard.repositorio.PerfilCandidatoRepositorio;
import br.com.joboard.repositorio.TokenRedefinicaoSenhaRepositorio;
import br.com.joboard.repositorio.TokenVerificacaoEmailRepositorio;
import br.com.joboard.repositorio.UsuarioRepositorio;
import br.com.joboard.repositorio.VagaRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Direitos do titular (LGPD): exclusão total da conta e exportação dos dados.
 */
@Service
@RequiredArgsConstructor
public class ContaServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PerfilCandidatoRepositorio perfilCandidatoRepositorio;
    private final CurriculoRepositorio curriculoRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final VagaRepositorio vagaRepositorio;
    private final CandidaturaRepositorio candidaturaRepositorio;
    private final HistoricoRepositorio historicoRepositorio;
    private final ContatoRepositorio contatoRepositorio;
    private final TokenVerificacaoEmailRepositorio tokenVerificacaoEmailRepositorio;
    private final TokenRedefinicaoSenhaRepositorio tokenRedefinicaoSenhaRepositorio;
    private final StorageServico storageServico;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void excluirConta(String senha) {
        Usuario usuario = SecurityUtils.getUsuarioLogado();

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new SenhaIncorretaException();
        }

        UUID usuarioId = usuario.getId();

        // Storage antes do banco (padrão do delete de currículo): se a remoção
        // do arquivo falhar, a conta permanece intacta e a operação pode ser repetida
        for (Curriculo curriculo : curriculoRepositorio.findAllByUsuarioId(usuarioId)) {
            storageServico.deletar(curriculo.getUrlArquivo());
        }

        // Ordem inversa da cadeia de FKs
        contatoRepositorio.deletarTodosDoUsuario(usuarioId);
        historicoRepositorio.deletarTodosDoUsuario(usuarioId);
        candidaturaRepositorio.deletarTodosDoUsuario(usuarioId);
        vagaRepositorio.deletarTodosDoUsuario(usuarioId);
        empresaRepositorio.deletarTodosDoUsuario(usuarioId);
        curriculoRepositorio.deletarTodosDoUsuario(usuarioId);
        perfilCandidatoRepositorio.deletarDoUsuario(usuarioId);
        tokenVerificacaoEmailRepositorio.deletarTodosDoUsuario(usuarioId);
        tokenRedefinicaoSenhaRepositorio.deletarTodosDoUsuario(usuarioId);
        usuarioRepositorio.delete(usuario);
    }

    @Transactional(readOnly = true)
    public byte[] exportarDados() {
        Usuario usuario = SecurityUtils.getUsuarioLogado();
        UUID usuarioId = usuario.getId();

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(saida)) {

            StringBuilder perfil = new StringBuilder(cabecalho("nomeCompleto", "telefone", "cidade",
                    "estado", "aceitaRemoto", "aceitaHibrido", "aceitaPresencial", "nivelExperiencia",
                    "disponibilidade", "pretensaoSalarialMin", "pretensaoSalarialMax", "urlLinkedin",
                    "urlGithub", "urlPortfolio", "resumoProfissional", "criadoEm"));
            perfilCandidatoRepositorio.findByUsuarioId(usuarioId)
                    .map(PerfilCandidatoResponseDTO::from)
                    .ifPresent(p -> perfil.append(linha(p.nomeCompleto(), p.telefone(), p.cidade(),
                            p.estado(), p.aceitaRemoto(), p.aceitaHibrido(), p.aceitaPresencial(),
                            p.nivelExperiencia(), p.disponibilidade(), p.pretensaoSalarialMin(),
                            p.pretensaoSalarialMax(), p.urlLinkedin(), p.urlGithub(), p.urlPortfolio(),
                            p.resumoProfissional(), p.criadoEm())));
            adicionarArquivo(zip, "perfil.csv", perfil.toString());

            StringBuilder empresas = new StringBuilder(cabecalho("id", "nome", "site", "localizacao",
                    "porte", "setor", "culturaObservacoes", "contatoRh", "contatoReferencia", "criadoEm"));
            for (EmpresaResponseDTO e : empresaRepositorio.findAllByUsuarioId(usuarioId)
                    .stream().map(EmpresaResponseDTO::from).toList()) {
                empresas.append(linha(e.id(), e.nome(), e.site(), e.localizacao(), e.porte(),
                        e.setor(), e.culturaObservacoes(), e.contatoRh(), e.contatoReferencia(), e.criadoEm()));
            }
            adicionarArquivo(zip, "empresas.csv", empresas.toString());

            StringBuilder vagas = new StringBuilder(cabecalho("id", "empresaNome", "titulo", "descricao",
                    "urlVaga", "localizacao", "modeloTrabalho", "tipoContrato", "nivelExperiencia",
                    "faixaSalarial", "beneficios", "requisitosObrigatorios", "requisitosDesejaveis",
                    "vagaAindaAberta", "criadoEm"));
            for (VagaResponseDTO v : vagaRepositorio.findAllByUsuarioId(usuarioId)
                    .stream().map(VagaResponseDTO::from).toList()) {
                vagas.append(linha(v.id(), v.empresaNome(), v.titulo(), v.descricao(), v.urlVaga(),
                        v.localizacao(), v.modeloTrabalho(), v.tipoContrato(), v.nivelExperiencia(),
                        v.faixaSalarial(), v.beneficios(), v.requisitosObrigatorios(),
                        v.requisitosDesejaveis(), v.vagaAindaAberta(), v.criadoEm()));
            }
            adicionarArquivo(zip, "vagas.csv", vagas.toString());

            StringBuilder candidaturas = new StringBuilder(cabecalho("id", "vagaTitulo", "empresaNome",
                    "status", "dataAplicacao", "plataformaAplicacao", "cartaApresentacao",
                    "portfolioEnviado", "proximaAcaoEm", "proximaAcaoDescricao",
                    "minhaAvaliacaoInteresse", "minhaAvaliacaoFit", "notas", "resultadoFinal",
                    "arquivada", "criadoEm"));
            for (CandidaturaResponseDTO c : candidaturaRepositorio.findAllByUsuarioId(usuarioId)
                    .stream().map(CandidaturaResponseDTO::from).toList()) {
                candidaturas.append(linha(c.id(), c.vagaTitulo(), c.empresaNome(), c.status(),
                        c.dataAplicacao(), c.plataformaAplicacao(), c.cartaApresentacao(),
                        c.portfolioEnviado(), c.proximaAcaoEm(), c.proximaAcaoDescricao(),
                        c.minhaAvaliacaoInteresse(), c.minhaAvaliacaoFit(), c.notas(),
                        c.resultadoFinal(), c.arquivada(), c.criadoEm()));
            }
            adicionarArquivo(zip, "candidaturas.csv", candidaturas.toString());

            StringBuilder historico = new StringBuilder(cabecalho("candidaturaId", "tipoEvento",
                    "statusAnterior", "statusNovo", "tituloEvento", "descricao", "dataEvento"));
            historicoRepositorio.findAllByCandidaturaUsuarioId(usuarioId).forEach(h ->
                    historico.append(linha(h.getCandidatura().getId(), h.getTipoEvento(),
                            h.getStatusAnterior(), h.getStatusNovo(), h.getTituloEvento(),
                            h.getDescricao(), h.getDataEvento())));
            adicionarArquivo(zip, "historico.csv", historico.toString());

            StringBuilder contatos = new StringBuilder(cabecalho("candidaturaId", "nome", "cargo",
                    "email", "telefone", "linkedin", "tipoContato", "interacaoPrincipal", "criadoEm"));
            for (ContatoResponseDTO c : contatoRepositorio.findAllByCandidaturaUsuarioId(usuarioId)
                    .stream().map(ContatoResponseDTO::from).toList()) {
                contatos.append(linha(c.candidaturaId(), c.nome(), c.cargo(), c.email(), c.telefone(),
                        c.linkedin(), c.tipoContato(), c.interacaoPrincipal(), c.criadoEm()));
            }
            adicionarArquivo(zip, "contatos.csv", contatos.toString());

            StringBuilder curriculos = new StringBuilder(cabecalho("nomeArquivo", "versao",
                    "ehPrincipal", "tamanhoBytes", "criadoEm"));
            for (CurriculoResponseDTO c : curriculoRepositorio.findAllByUsuarioId(usuarioId)
                    .stream().map(CurriculoResponseDTO::from).toList()) {
                curriculos.append(linha(c.nomeArquivo(), c.versao(), c.ehPrincipal(),
                        c.tamanhoBytes(), c.criadoEm()));
            }
            adicionarArquivo(zip, "curriculos.csv", curriculos.toString());

        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gerar exportação de dados", e);
        }

        return saida.toByteArray();
    }

    private String cabecalho(Object... colunas) {
        return linha(colunas);
    }

    private String linha(Object... valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) sb.append(',');
            String valor = valores[i] == null ? "" : valores[i].toString();
            sb.append('"').append(valor.replace("\"", "\"\"")).append('"');
        }
        return sb.append('\n').toString();
    }

    private void adicionarArquivo(ZipOutputStream zip, String nome, String conteudo) throws IOException {
        zip.putNextEntry(new ZipEntry(nome));
        // BOM UTF-8 para acentos abrirem corretamente no Excel
        zip.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        zip.write(conteudo.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
