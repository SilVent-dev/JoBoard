package br.com.joboard.seguranca;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting por IP nos endpoints públicos sensíveis a abuso automatizado
 * (cadastro e esqueci-senha). Limites em memória — zeram a cada restart,
 * suficiente para instância única. O limite por email fica no UsuarioServico.
 * Responde 429 direto: exceções de filtro não chegam ao GlobalExceptionHandler.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMITE_CADASTRO_POR_HORA = 5;
    private static final int LIMITE_ESQUECI_SENHA_POR_HORA = 5;
    // Proteção contra crescimento sem limite do mapa (bots com muitos IPs)
    private static final int MAXIMO_CHAVES_EM_MEMORIA = 10_000;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !path.equals("/auth/cadastro") && !path.equals("/auth/esqueci-senha");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        int limite = path.equals("/auth/cadastro")
                ? LIMITE_CADASTRO_POR_HORA
                : LIMITE_ESQUECI_SENHA_POR_HORA;

        if (buckets.size() > MAXIMO_CHAVES_EM_MEMORIA) {
            buckets.clear();
        }

        Bucket bucket = buckets.computeIfAbsent(path + "|" + extrairIp(request),
                chave -> Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(limite)
                                .refillGreedy(limite, Duration.ofHours(1))
                                .build())
                        .build());

        if (!bucket.tryConsume(1)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"mensagem\":\"Muitas tentativas. Aguarde um pouco e tente novamente.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extrairIp(HttpServletRequest request) {
        // Atrás de proxy (Railway/Render), o IP real vem no X-Forwarded-For
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
