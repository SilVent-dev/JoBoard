package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.PerfilCandidatoRequestDTO;
import br.com.joboard.dominio.DTO.PerfilCandidatoResponseDTO;
import br.com.joboard.dominio.entidade.PerfilCandidato;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.excecao.RecursoNaoEncontradoException;
import br.com.joboard.repositorio.PerfilCandidatoRepositorio;
import br.com.joboard.seguranca.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PerfilCandidatoServico {

    private final PerfilCandidatoRepositorio perfilCandidatoRepositorio;

    public PerfilCandidatoServico(PerfilCandidatoRepositorio perfilCandidatoRepositorio) {
        this.perfilCandidatoRepositorio = perfilCandidatoRepositorio;
    }

    public PerfilCandidatoResponseDTO buscar(){
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();
        PerfilCandidato perfil = perfilCandidatoRepositorio.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(()-> new RecursoNaoEncontradoException("PerfilCandidato", usuarioLogado.getId()));

        return PerfilCandidatoResponseDTO.from(perfil);
    }

    @Transactional
    public PerfilCandidatoResponseDTO salvar (PerfilCandidatoRequestDTO dados){
        Usuario usuarioLogado = SecurityUtils.getUsuarioLogado();

        validarCpfUnico(dados.cpf(), usuarioLogado.getId());

        if (dados.pretensaoSalarialMin() != null && dados.pretensaoSalarialMax() != null) {
            if (dados.pretensaoSalarialMin().compareTo(dados.pretensaoSalarialMax()) >= 0) {
                throw new IllegalArgumentException("Pretensão mínima deve ser menor que a máxima.");
            }
        }

        PerfilCandidato perfil = perfilCandidatoRepositorio
                .findByUsuarioId(usuarioLogado.getId())
                .orElseGet(() -> PerfilCandidato.builder()
                        .usuario(usuarioLogado)
                        .build());

        perfil.setNomeCompleto(dados.nomeCompleto());
        perfil.setCpf(dados.cpf());
        perfil.setTelefone(dados.telefone());
        perfil.setCidade(dados.cidade());
        perfil.setEstado(dados.estado());
        perfil.setAceitaRemoto(dados.aceitaRemoto() != null ? dados.aceitaRemoto() : true);
        perfil.setAceitaHibrido(dados.aceitaHibrido() != null ? dados.aceitaHibrido() : true);
        perfil.setAceitaPresencial(dados.aceitaPresencial() != null ? dados.aceitaPresencial() : true);
        perfil.setNivelExperiencia(dados.nivelExperiencia());
        perfil.setDisponibilidade(dados.disponibilidade());
        perfil.setPretensaoSalarialMin(dados.pretensaoSalarialMin());
        perfil.setPretensaoSalarialMax(dados.pretensaoSalarialMax());
        perfil.setUrlLinkedin(dados.urlLinkedin());
        perfil.setUrlGithub(dados.urlGithub());
        perfil.setUrlPortfolio(dados.urlPortfolio());
        perfil.setResumoProfissional(dados.resumoProfissional());

        return PerfilCandidatoResponseDTO.from(perfilCandidatoRepositorio.save(perfil));
    }

    private void validarCpfUnico(String cpf, UUID usuarioId) {
        if(perfilCandidatoRepositorio.existsByCpfAndUsuarioIdNot(cpf, usuarioId)){
            throw new IllegalStateException("Não foi possível completar o cadastro.");
        }
    }
}
