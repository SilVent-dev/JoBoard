package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlataformaAplicacaoEnum {

    LINKEDIN("LINKEDIN"),
    INDEED("Indeed"),
    GUPY("Gupy"),
    GLASSDOOR("Glassdoor"),
    SITE_EMPRESA("Site específico da Empresa"),
    INDICACAO("Indicacao de vaga"),
    EMAIL_DIRETO("E-mail direto da empresa"),
    OUTRO("Outro formato (descreva qual)");

    private final String descricao;
}