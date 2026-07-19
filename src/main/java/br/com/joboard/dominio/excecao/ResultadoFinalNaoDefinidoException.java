package br.com.joboard.dominio.excecao;

public class ResultadoFinalNaoDefinidoException extends RuntimeException {
    public ResultadoFinalNaoDefinidoException() {
        super("Não é possível arquivar uma candidatura sem resultado final definido.");
    }
}
