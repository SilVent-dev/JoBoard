package br.com.joboard.dominio.excecao;

public class VagaFechadaException extends RuntimeException {
    public VagaFechadaException() {
        super("Não é possível se candidatar a uma vaga fechada.");
    }
}
