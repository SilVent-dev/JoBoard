package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.AutenticacaDTO;
import br.com.joboard.dominio.DTO.CadastroDTO;
import br.com.joboard.dominio.DTO.LoginRespostaDTO;
import br.com.joboard.dominio.entidade.TipoUsuario;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.TipoUsuarioEnum;
import br.com.joboard.repositorio.TipoUsuarioRepositorio;
import br.com.joboard.repositorio.UsuarioRepositorio;
import br.com.joboard.servico.TokenServico;
import br.com.joboard.servico.UsuarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("auth")
public class AutenticacaoControlador {

    private final AuthenticationManager authenticationManager;
    private final TokenServico tokenServico;
    private final UsuarioServico usuarioServico;

    public AutenticacaoControlador(AuthenticationManager authenticationManager,
                                   UsuarioRepositorio usuarioRepositorio,
                                   TipoUsuarioRepositorio tipoUsuarioRepositorio, TokenServico tokenServico, PasswordEncoder passwordEncoder, UsuarioServico usuarioServico) {
        this.authenticationManager = authenticationManager;
        this.tokenServico = tokenServico;
        this.usuarioServico = usuarioServico;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDTO> login (@RequestBody @Valid AutenticacaDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senhaHash());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenServico.generateToken((Usuario) auth.getPrincipal());
        return ResponseEntity.ok(new LoginRespostaDTO(token));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Void> cadastro (@RequestBody @Valid CadastroDTO data){
        usuarioServico.cadastrar(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
