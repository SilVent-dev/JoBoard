package br.com.joboard.dominio.excecao;

public class SenhaIncorretaException extends RuntimeException {
    public SenhaIncorretaException() {
        super("Senha incorreta.");
    }
}
