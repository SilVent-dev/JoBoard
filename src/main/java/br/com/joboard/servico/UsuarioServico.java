package br.com.joboard.servico;

import br.com.joboard.dominio.DTO.CadastroDTO;
import br.com.joboard.dominio.entidade.TipoUsuario;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.TipoUsuarioEnum;
import br.com.joboard.repositorio.TipoUsuarioRepositorio;
import br.com.joboard.repositorio.UsuarioRepositorio;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServico(UsuarioRepositorio usuarioRepositorio,
                                   TipoUsuarioRepositorio tipoUsuarioRepositorio,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public void cadastrar (CadastroDTO data){
        if(this.usuarioRepositorio.findByEmail(data.email()).isPresent()){
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        TipoUsuario tipoUsuario = tipoUsuarioRepositorio.findByNome(TipoUsuarioEnum.USUARIO)
                .orElseThrow(()-> new RuntimeException("Tipo de usuário não encontrado"));

        Usuario novoUsuario = Usuario.builder()
                .email(data.email())
                .senhaHash(passwordEncoder.encode(data.senhaHash()))
                .nome(data.nome())
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .tipoUsuario(List.of(tipoUsuario))
                .build();

        this.usuarioRepositorio.save(novoUsuario);
    }

}
