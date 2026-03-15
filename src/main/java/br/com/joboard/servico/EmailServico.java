package br.com.joboard.servico;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServico {

    private final JavaMailSender mailSender;

    @Value("${app.url.base}")
    private String urlBase;

    public EmailServico(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void enviarVerificacao(String destinatario, String token){
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Confirme seu cadastro - JobTracker");
        mensagem.setText(
                "Olá! Bem-vindo ao JobTracker.\n\n" +
                "Clique no link abaixo para ativar sua conta:\n\n" +
                 urlBase + "/auth/verificar-email?token=" + token + "\n\n" +
                 "O link é válido por 2 horas.\n\n" +
                 "Se você não criou essa conta, ignore este email."
        );
        mailSender.send(mensagem);
    }
}
