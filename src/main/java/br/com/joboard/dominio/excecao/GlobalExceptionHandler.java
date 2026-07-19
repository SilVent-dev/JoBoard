package br.com.joboard.dominio.excecao;

import br.com.joboard.dominio.evento.ContaPendenteEvento;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApplicationEventPublisher eventPublisher;

    public GlobalExceptionHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @ExceptionHandler(ContaPendenteExcecao.class)
    public ResponseEntity<Map<String, String>> handleContaPendente(ContaPendenteExcecao ex) {
        return responderContaPendente(ex.getEmail());
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Map<String, String>> handleInternalAuth(InternalAuthenticationServiceException ex) {
        if (ex.getCause() instanceof ContaPendenteExcecao contaPendente) {
            return responderContaPendente(contaPendente.getEmail());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensagem", "Erro de autenticação."));
    }

    private ResponseEntity<Map<String, String>> responderContaPendente(String email) {
        eventPublisher.publishEvent(new ContaPendenteEvento(email));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensagem", "Email de verificação reenviado. Confirme seu cadastro para continuar."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensagem", mensagem.isBlank() ? "Requisição inválida." : mensagem));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensagem", "Email ou senha incorretos."));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, String>> handleContaBloqueada(LockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensagem", "Conta bloqueada. Entre em contato com o suporte."));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleContaDesativada(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensagem", "Conta desativada."));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<Map<String, String>> handleEmailJaCadastrado(EmailJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, String>> handleAcessoNegado(AcessoNegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(EmpresaJaCadastradaException.class)
    public ResponseEntity<Map<String, String>> handleEmpresaJaCadastrada(
            EmpresaJaCadastradaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(VagaFechadaException.class)
    public ResponseEntity<Map<String, String>> handleVagaFechada(VagaFechadaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(CandidaturaDuplicadaException.class)
    public ResponseEntity<Map<String, String>> handleCandidaturaDuplicada(
            CandidaturaDuplicadaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<Map<String, String>> handleTransicaoInvalida(
            TransicaoStatusInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(TokenRedefinicaoInvalidoException.class)
    public ResponseEntity<Map<String, String>> handleTokenRedefinicaoInvalido(
            TokenRedefinicaoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(TokenVerificacaoInvalidoException.class)
    public ResponseEntity<Map<String, String>> handleTokenVerificacaoInvalido(
            TokenVerificacaoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(ResultadoFinalNaoDefinidoException.class)
    public ResponseEntity<Map<String, String>> handleResultadoFinalNaoDefinido(
            ResultadoFinalNaoDefinidoException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(PretensaoSalarialInvalidaException.class)
    public ResponseEntity<Map<String, String>> handlePretensaoSalarialInvalida(
            PretensaoSalarialInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(SenhaIncorretaException.class)
    public ResponseEntity<Map<String, String>> handleSenhaIncorreta(SenhaIncorretaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<Map<String, String>> handleArquivoInvalido(ArquivoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(VersaoCurriculoDuplicadaException.class)
    public ResponseEntity<Map<String, String>> handleVersaoCurriculoDuplicada(
            VersaoCurriculoDuplicadaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensagem", "Erro interno. Tente novamente mais tarde."));
    }

}
