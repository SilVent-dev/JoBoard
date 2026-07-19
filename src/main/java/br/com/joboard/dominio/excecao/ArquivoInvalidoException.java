package br.com.joboard.dominio.excecao;

public class ArquivoInvalidoException extends RuntimeException {
    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
