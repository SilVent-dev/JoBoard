package br.com.joboard.seguranca;

import br.com.joboard.dominio.entidade.TipoUsuario;
import br.com.joboard.dominio.entidade.Usuario;
import br.com.joboard.dominio.enums.StatusContaEnum;
import br.com.joboard.dominio.enums.TipoUsuarioEnum;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private Usuario usuario;

    public UserDetailsImpl(Usuario usuario){
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*
         Este metodo converte a lista de papéis (roles) associados ao usuário
         em uma coleção de GrantedAuthorities, que é a forma que o Spring Security
         usa para representar papéis. Isso é feito mapeando cada papel para um
         novo SimpleGrantedAuthority, que é uma implementação simples de
         GrantedAuthority
        */
            return usuario.getTipoUsuario().stream()
                    .map(tipo -> new SimpleGrantedAuthority(tipo.getNome().name()))
                    .toList();

        }

    @Override
    public @Nullable String getPassword() {
        return usuario.getSenhaHash();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return usuario.getStatusConta() != StatusContaEnum.BLOQUEADO;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return usuario.podeAutenticar();
    }
}
