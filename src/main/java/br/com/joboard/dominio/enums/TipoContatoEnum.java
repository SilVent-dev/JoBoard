package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoContatoEnum {

    RECRUTADOR("Recrutador"),
    TECH_LEAD("Tech Lead"),
    GESTOR_DIRETO("Gestor direto"),
    RH("RH"),
    COLEGA_EQUIPE("Colega de equipe"),
    FUNDADOR("Fundador");

    private final String descricao;
}