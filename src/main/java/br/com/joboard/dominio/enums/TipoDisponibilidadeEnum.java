package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoDisponibilidadeEnum {

    IMEDIATA("Imediata"),
    DIAS_15("15 Dias"),
    DIAS_30("30 Dias"),
    DIAS_60("60 Dias"),
    SONDANDO_MERCADO("Sondando mercado");

    private final String descricao;
}