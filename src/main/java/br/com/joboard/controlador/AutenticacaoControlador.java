package br.com.joboard.controlador;

import br.com.joboard.dominio.DTO.AutenticacaDTO;
import br.com.joboard.dominio.DTO.CadastroDTO;
import br.com.joboard.dominio.DTO.LoginRespostaDTO;
import br.com.joboard.seguranca.UserDetailsImpl;
import br.com.joboard.servico.TokenServico;
import br.com.joboard.servico.UsuarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AutenticacaoControlador {

    private final AuthenticationManager authenticationManager;
    private final TokenServico tokenServico;
    private final UsuarioServico usuarioServico;

    public AutenticacaoControlador(AuthenticationManager authenticationManager,
                                   TokenServico tokenServico,
                                   UsuarioServico usuarioServico) {
        this.authenticationManager = authenticationManager;
        this.tokenServico = tokenServico;
        this.usuarioServico = usuarioServico;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDTO> login (@RequestBody @Valid AutenticacaDTO data){
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        usuarioServico.registrarLogin(userDetails.getUsuario());
        var token = tokenServico.generateToken(userDetails.getUsuario());
        return ResponseEntity.ok(new LoginRespostaDTO(token));
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Void> cadastro (@RequestBody @Valid CadastroDTO data){
        usuarioServico.cadastrar(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verificar-email")
    public ResponseEntity<Map<String, String>> verificarEmail(@RequestParam String token){
        usuarioServico.verificarEmail(token);
        return ResponseEntity.ok(Map.of("mensagem", "Email confirmado com sucesso! Você já pode fazer login."));
    }
}
