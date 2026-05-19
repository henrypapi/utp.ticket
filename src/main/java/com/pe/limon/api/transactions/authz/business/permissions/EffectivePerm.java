package com.pe.limon.api.transactions.authz.business.permissions;

import java.util.Collections;
import java.util.Set;


public record EffectivePerm(
        long eventId,
        String eventName,
        String evenDatetime,
        String paymentMode,
        String timeZone,
        boolean qrEnabled,
        boolean isOwner,
        boolean isAdmin,
        Set<String> modules
) {
    public boolean hasModule(String module) {
        return isAdmin || (modules != null && modules.contains(module));
    }
    public static EffectivePerm empty(long eventId) {
        return new EffectivePerm(eventId, "",null,"","",false,false, false, Collections.emptySet());
    }
}
