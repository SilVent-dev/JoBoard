package br.com.joboard.dominio.excecao;

public class VersaoCurriculoDuplicadaException extends RuntimeException {
    public VersaoCurriculoDuplicadaException(String versao) {
        super("Já existe um currículo com a versão '" + versao + "'.");
    }
}