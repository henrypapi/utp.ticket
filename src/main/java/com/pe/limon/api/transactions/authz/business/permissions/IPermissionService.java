package com.pe.limon.api.transactions.authz.business.permissions;

import java.util.Map;

public interface IPermissionService {
    EffectivePerm getEffective(long eventId, String userId);

    boolean can(long eventId, String userId, String module);

    void invalidate(long eventId, String userId);

    Map<Long, EffectivePerm> getAllEffective(String userId);
}
