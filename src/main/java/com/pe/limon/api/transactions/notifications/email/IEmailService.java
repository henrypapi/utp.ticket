package com.pe.limon.api.transactions.notifications.email;

import com.pe.limon.api.transactions.notifications.email.dto.EmailRequest;

public interface IEmailService {
    void send(EmailRequest request);
}
