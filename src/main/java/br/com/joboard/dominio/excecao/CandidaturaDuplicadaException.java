package br.com.joboard.dominio.excecao;

public class CandidaturaDuplicadaException extends RuntimeException {
    public CandidaturaDuplicadaException() {
        super("Você já possui uma candidatura ativa para esta vaga.");
    }
}