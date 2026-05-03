package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PorteEmpresaEnum {

    STARTUP("Startup"),
    PEQUENA("Pequena"),
    MEDIA("Média"),
    GRANDE("Grande"),
    MULTINACIONAL("Multinacional");

    private final String descricao;
}