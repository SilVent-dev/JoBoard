package br.com.joboard.seguranca;

import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.repositorio.UsuarioRepositorio;
import br.com.joboard.servico.TokenServico;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenServico tokenServico;
    private final UsuarioRepositorio usuarioRepositorio;

    public SecurityFilter(TokenServico tokenServico, UsuarioRepositorio usuarioRepositorio) {
        this.tokenServico = tokenServico;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recuperarToken(request);
        if (token !=null){
            var login = tokenServico.validarToken(token);
            Usuario usuario = usuarioRepositorio.findByEmail(login).orElseThrow(()-> new RuntimeException("Usuário não encontrado!"));
            UserDetails userDetails = new UserDetailsImpl(usuario);

            var autenticacao = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader==null) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}
