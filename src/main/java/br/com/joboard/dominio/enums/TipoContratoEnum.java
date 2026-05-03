package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoContratoEnum {

    CLT("CLT"),
    PJ("PJ"),
    ESTAGIO("Estágio"),
    TEMPORARIO("Temporário"),
    FREELANCE("Freelance");

    private final String descricao;
}