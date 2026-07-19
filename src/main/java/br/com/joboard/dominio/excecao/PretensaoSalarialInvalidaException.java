package br.com.joboard.dominio.excecao;

public class PretensaoSalarialInvalidaException extends RuntimeException {
    public PretensaoSalarialInvalidaException() {
        super("Pretensão mínima deve ser menor que a máxima.");
    }
}
