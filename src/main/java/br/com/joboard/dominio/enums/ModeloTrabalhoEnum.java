package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModeloTrabalhoEnum {

    REMOTO("Remoto"),
    HIBRIDO("Híbrido"),
    PRESENCIAL("Presencial");

    private final String descricao;
}