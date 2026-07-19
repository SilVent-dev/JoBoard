package br.com.joboard.listener;

import br.com.joboard.dominio.evento.SolicitacaoRedefinicaoSenhaEvento;
import br.com.joboard.servico.EmailServico;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RedefinicaoSenhaListener {

    private final EmailServico emailServico;

    public RedefinicaoSenhaListener(EmailServico emailServico) {
        this.emailServico = emailServico;
    }

    @EventListener
    public void handle(SolicitacaoRedefinicaoSenhaEvento evento) {
        emailServico.enviarRedefinicaoSenha(evento.email(), evento.nome(), evento.token());
    }
}
