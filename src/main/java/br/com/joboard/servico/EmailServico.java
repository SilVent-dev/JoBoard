package br.com.joboard.servico;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServico {

    private static final Logger log = LoggerFactory.getLogger(EmailServico.class);

    private final JavaMailSender mailSender;

    @Value("${app.url.base}")
    private String urlBase;

    @Value("${app.url.frontend}")
    private String urlFrontend;

    // Remetente (From). Vazio em dev (usa o padrão do SMTP); em producao com Resend
    // deve ser onboarding@resend.dev ou um endereco de dominio verificado.
    @Value("${app.mail.from:}")
    private String remetente;

    public EmailServico(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    public void enviarVerificacao(String destinatario, String nomeUsuario, String token){
        try {
            // aponta para a tela do frontend, que consome GET /auth/verificar-email
            String link = urlFrontend + "/verificar-email?token=" + token;
            String html = carregarTemplate("templates/verificacao-email.html")
                    .replace("{{nome}}", nomeUsuario).replace("{{link}}", link);

            enviar(destinatario, "Confirme seu cadastro - Joboard", html);
        } catch (Exception e){
            log.error("Falha ao enviar email de verificação para {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar email de verificação", e);
        }

    }

    public void enviarRedefinicaoSenha(String destinatario, String nomeUsuario, String token){
        try {
            String link = urlFrontend + "/redefinir-senha?token=" + token;
            String html = carregarTemplate("templates/redefinicao-senha.html")
                    .replace("{{nome}}", nomeUsuario).replace("{{link}}", link);

            enviar(destinatario, "Redefinição de senha - Joboard", html);
        } catch (Exception e){
            throw new RuntimeException("Erro ao enviar email de redefinição de senha", e);
        }
    }

    public void enviarFollowupDiario(String destinatario, String nomeUsuario, String itensHtml){
        try {
            String html = carregarTemplate("templates/followup-diario.html")
                    .replace("{{nome}}", nomeUsuario)
                    .replace("{{itens}}", itensHtml)
                    .replace("{{link}}", urlFrontend);

            enviar(destinatario, "Seus follow-ups de hoje - Joboard", html);
        } catch (Exception e){
            throw new RuntimeException("Erro ao enviar email de follow-up", e);
        }
    }

    private void enviar(String destinatario, String assunto, String html) throws Exception {
        MimeMessage mensagem = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");
        if (remetente != null && !remetente.isBlank()) {
            helper.setFrom(remetente);
        }
        helper.setTo(destinatario);
        helper.setSubject(assunto);
        helper.setText(html, true);

        mailSender.send(mensagem);
    }

    public String carregarTemplate(String caminho) throws Exception{
        ClassPathResource resource = new ClassPathResource(caminho);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
