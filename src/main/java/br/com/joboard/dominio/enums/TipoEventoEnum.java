package br.com.joboard.dominio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoEventoEnum {

    MUDANCA_STATUS("Mudança de status"),
    ENTREVISTA_AGENDADA("Entrevista agendada"),
    ENTREVISTA_REALIZADA("Entrevista realizada"),
    TESTE_ENVIADO("Teste enviado"),
    FEEDBACK_RECEBIDO("Feedback recebido"),
    FOLLOW_UP_REALIZADO("Follow-up realizado"),
    NOTA_ADICIONADA("Nota adicionada");

    private final String descricao;
}