package br.com.joboard.dominio.excecao;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " não encontrado: " + id);
    }
}
