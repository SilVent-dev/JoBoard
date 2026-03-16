package br.com.joboard.dominio.excecao;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Acesso negado.");
    }
}
