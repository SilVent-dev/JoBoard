package br.com.joboard.listener;

import br.com.joboard.dominio.evento.ContaPendenteEvento;
import br.com.joboard.servico.UsuarioServico;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class VerificacaoEmailListener {

    private final UsuarioServico usuarioServico;

    public VerificacaoEmailListener(UsuarioServico usuarioServico) {
        this.usuarioServico = usuarioServico;
    }

    @EventListener
    public void handle(ContaPendenteEvento evento) {
        usuarioServico.reenviarVerificacaoPorEmail(evento.email());
    }
}
