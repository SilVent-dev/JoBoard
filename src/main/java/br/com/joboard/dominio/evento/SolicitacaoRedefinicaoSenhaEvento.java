package br.com.joboard.dominio.evento;

public record SolicitacaoRedefinicaoSenhaEvento(String email, String nome, String token) {
}
