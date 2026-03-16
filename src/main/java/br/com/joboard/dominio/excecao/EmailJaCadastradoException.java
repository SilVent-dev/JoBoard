package br.com.joboard.dominio.excecao;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(){
        super("Email ja cadastrado!");
    }

}
