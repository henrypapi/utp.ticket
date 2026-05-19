package com.pe.limon.api.transactions.notifications.email.dto;

import com.pe.limon.api.transactions.notifications.email.enums.EmailTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record EmailRequest(
        String to,
        String subject,                   // si null, se resuelve por plantilla + i18n
        EmailTemplate template,
        Map<String, Object> variables,    // variables Thymeleaf
        List<EmailAttachment> attachments,
        Locale locale                     // es-PE, es, en, etc.
) {
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String to, subject;
        private EmailTemplate template;
        private Map<String,Object> variables = Map.of();
        private List<EmailAttachment> attachments = List.of();
        private Locale locale = Locale.getDefault();
        public Builder to(String v){ this.to=v; return this; }
        public Builder subject(String v){ this.subject=v; return this; }
        public Builder template(EmailTemplate v){ this.template=v; return this; }
        public Builder variables(Map<String,Object> v){ this.variables=v; return this; }
        public Builder attachments(List<EmailAttachment> v){ this.attachments=v; return this; }
        public Builder locale(Locale v){ this.locale=v; return this; }
        public EmailRequest build(){ return new EmailRequest(to, subject, template, variables, attachments, locale); }
    }
}