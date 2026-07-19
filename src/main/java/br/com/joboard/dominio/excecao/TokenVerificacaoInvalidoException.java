package br.com.joboard.dominio.excecao;

public class TokenVerificacaoInvalidoException extends RuntimeException {
    public TokenVerificacaoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
