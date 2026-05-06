package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusProcessoSeletivoEnum {

    LISTA_DESEJO("Lista de desejo"),
    APLICADA("Aplicada"),
    TRIAGEM_TELEFONICA("Triagem telefônica"),
    ENTREVISTA_TECNICA("Entrevista técnica"),
    ENTREVISTA_COMPORTAMENTAL("Entrevista comportamental"),
    TESTE_PRATICO("Teste prático"),
    PROPOSTA_RECEBIDA("Proposta recebida"),
    ACEITA("Aceita"),
    REJEITADA("Rejeitada"),
    DESISTIDA("Desistida");

    private final String descricao;
}