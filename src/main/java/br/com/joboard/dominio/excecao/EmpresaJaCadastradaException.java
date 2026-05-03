package br.com.joboard.dominio.excecao;

public class EmpresaJaCadastradaException extends RuntimeException {
    public EmpresaJaCadastradaException(String nome) {
        super("Já existe uma empresa com o nome '" + nome + "' no seu catálogo.");
    }
}
