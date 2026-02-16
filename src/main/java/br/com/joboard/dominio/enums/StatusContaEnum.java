package br.com.joboard.dominio.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusContaEnum {

    ATIVO("Ativo"),
    INATIVO("Inativo"),
    BLOQUEADO("Bloqueado"),
    EXCLUIDO("Excluído"),
    CADASTRO_PENDENTE_CONFIRMACAO("Cadastro Pendente de Confirmação");

    private final String descricao;
}