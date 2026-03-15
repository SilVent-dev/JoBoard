package br.com.joboard.servico;

import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusContaEnum;
import br.com.joboard.dominio.excecao.ContaPendenteExcecao;
import br.com.joboard.repositorio.UsuarioRepositorio;
import br.com.joboard.seguranca.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutorizacaoServico implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public AutorizacaoServico(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario =  usuarioRepositorio.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException ("Usuário não encontrado!"));

        if (usuario.getStatusConta() == StatusContaEnum.CADASTRO_PENDENTE_CONFIRMACAO){
            throw new ContaPendenteExcecao(usuario.getEmail());
        }
        return new UserDetailsImpl(usuario);
    }
}
