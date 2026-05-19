package com.pe.limon.api.transactions.authz.business.profile.async.listener;

import com.pe.limon.api.transactions.authz.business.profile.async.ProfileImageAsyncService;
import com.pe.limon.api.transactions.authz.business.profile.async.dto.ProfileImageRequestedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProfileImageListener {

    private final ProfileImageAsyncService asyncService;

    public ProfileImageListener(ProfileImageAsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProfileImageRequestedEvent ev) {
        asyncService.process(ev.fileName(),ev.bytes());
    }
}
