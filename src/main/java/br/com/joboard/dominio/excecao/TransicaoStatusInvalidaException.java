package br.com.joboard.dominio.excecao;

import br.com.joboard.dominio.enums.StatusProcessoSeletivoEnum;

public class TransicaoStatusInvalidaException extends RuntimeException {
    public TransicaoStatusInvalidaException(
            StatusProcessoSeletivoEnum atual,
            StatusProcessoSeletivoEnum tentativa) {
        super("Transição inválida: " + atual.getDescricao()
                + " → " + tentativa.getDescricao());
    }
}