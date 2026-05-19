package com.pe.limon.api.transactions.notifications.email.impl;

import com.pe.limon.api.transactions.notifications.email.IEmailService;
import com.pe.limon.api.transactions.notifications.email.dto.EmailAttachment;
import com.pe.limon.api.transactions.notifications.email.dto.EmailRequest;
import com.pe.limon.api.transactions.notifications.email.enums.EmailTemplate;
import jakarta.annotation.Nullable;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Service
public class DefaultEmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messages;
    private final String from;
    private final boolean asyncEnabled;

    public DefaultEmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            MessageSource messages,
            @Value("${app.mail.from}") String from,
            @Value("${app.mail.enable-async:true}") boolean asyncEnabled
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.messages = messages;
        this.from = from;
        this.asyncEnabled = asyncEnabled;
    }

    @Override
    public void send(EmailRequest request) {
        if (asyncEnabled) {
            sendAsync(request);
        } else {
            doSend(request);
        }
    }

    @Async
    protected void sendAsync(EmailRequest request) {
        doSend(request);
    }

    private void doSend(EmailRequest req) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(req.to());

            String subject = resolveSubject(req.template(), req.subject(), req.locale());
            helper.setSubject(subject);

            String html = renderTemplate(req.template(), req.variables(), req.locale());
            helper.setText(html, true);

            if (req.attachments() != null) {
                for (EmailAttachment a : req.attachments()) {
                    if (a.inline()) {
                        helper.addInline(a.contentId(), new org.springframework.core.io.ByteArrayResource(a.content()), a.contentType());
                    } else {
                        helper.addAttachment(a.filename(), new org.springframework.core.io.ByteArrayResource(a.content()));
                    }
                }
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            // aquí puedes loguear, enviar a dead-letter, o reintentar con Spring Retry
            throw new RuntimeException("Error enviando email", e);
        }
    }

    private String renderTemplate(EmailTemplate template, Map<String,Object> variables, Locale locale) {
        Context ctx = new Context(locale);
        if (variables != null) variables.forEach(ctx::setVariable);
        // variables comunes (logo, support, etc.)
        ctx.setVariable("appName", "Lim-On");
        ctx.setVariable("supportEmail", "soporte@lim-on.social");
        return templateEngine.process(template.key(), ctx); // p.ej. "verify-email"
    }

    private String resolveSubject(EmailTemplate tmpl, @Nullable String custom, Locale locale) {
        if (custom != null && !custom.isBlank()) return custom;
        // usa i18n: messages.properties
        String code = "email.subject." + tmpl.key(); // ej: email.subject.verify-email
        return messages.getMessage(code, null, defaultSubject(tmpl), locale);
    }

    private String defaultSubject(EmailTemplate t) {
        return switch (t) {
            case PURCHASE_RECEIPT -> "Tu comprobante de compra";
            case TICKET_DELIVERY -> "Tus entradas";
            case RESET_PASSWORD -> "Restablecer contraseña";
            case VERIFY_EMAIL -> "Verifica tu correo";
            case GENERIC_INFO -> "Información";
        };
    }
}