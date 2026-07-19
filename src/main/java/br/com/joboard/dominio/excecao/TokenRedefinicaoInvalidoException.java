package br.com.joboard.dominio.excecao;

public class TokenRedefinicaoInvalidoException extends RuntimeException {
    public TokenRedefinicaoInvalidoException() {
        // Mensagem única para token inexistente/usado/expirado — não revela o motivo
        super("Link de redefinição inválido ou expirado. Solicite um novo.");
    }
}
