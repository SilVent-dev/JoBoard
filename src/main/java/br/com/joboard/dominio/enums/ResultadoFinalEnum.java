package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultadoFinalEnum {

    APROVADO("Aprovado"),
    REPROVADO("Reprovado"),
    DESISTI("Desisti"),
    SEM_RESPOSTA("Sem resposta");

    private final String descricao;
}