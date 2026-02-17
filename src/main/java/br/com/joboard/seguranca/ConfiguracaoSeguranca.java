package br.com.joboard.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    // Rotas acessíveis sem autenticação (login e cadastro)
    private static final String[] ROTAS_PUBLICAS = {
            "/auth/cadastro",
            "/auth/login"
    };

    // Rotas acessíveis apenas por administradores
    private static final String[] ROTAS_ADMIN = {
            "/admin/**"
    };

    private final SecurityFilter securityFilter;

    public ConfiguracaoSeguranca (SecurityFilter securityFilter){
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, SecurityFilter securityFilter) throws Exception{
        return httpSecurity
                // Desativa CSRF pois usamos JWT (stateless, sem sessão)
                .csrf(AbstractHttpConfigurer::disable)
                // Define que a aplicação não mantém sessão — cada requisição é independente
                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize->authorize
                        // Rotas públicas: qualquer um pode acessar
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        // Rotas admin: apenas usuários com role ADMIN
                        .requestMatchers(ROTAS_ADMIN).hasRole("ADMIN")
                        // Qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
