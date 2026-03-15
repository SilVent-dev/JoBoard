package br.com.joboard.dominio.excecao;

public class ContaPendenteExcecao extends RuntimeException{
    private final String email;

    public ContaPendenteExcecao(String email){
        super("Conta pendente de confirmação.");
        this.email = email;
    }

    public String getEmail(){
        return email;
    }
}
