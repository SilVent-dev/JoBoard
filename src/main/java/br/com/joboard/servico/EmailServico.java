package br.com.joboard.servico;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServico {

    private final JavaMailSender mailSender;

    @Value("${app.url.base}")
    private String urlBase;

    public EmailServico(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void enviarVerificacao(String destinatario, String nomeUsuario, String token){
        try {
            String link = urlBase + "/auth/verificar-email?token=" + token;
            String html = carregarTemplate().replace("{{nome}}", nomeUsuario).replace("{{link}}", link);

            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject("Confirme seu cadastro - Joboard");
            helper.setText(html, true);

            mailSender.send(mensagem);
        } catch (Exception e){
            throw new RuntimeException("Erro ao enviar email de verificação", e);
        }

    }

    public String carregarTemplate() throws Exception{
        ClassPathResource resource = new ClassPathResource("templates/verificacao-email.html");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
