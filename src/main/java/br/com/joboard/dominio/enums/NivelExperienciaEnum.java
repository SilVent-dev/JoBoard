package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NivelExperienciaEnum {

    ESTAGIARIO("Estagiario"),
    JUNIOR("Junior"),
    PLENO("Pleno"),
    SENIOR("Senior"),
    ESPECIALISTA("Especialista"),
    STAFF("Staff");

    private final String descricao;
}