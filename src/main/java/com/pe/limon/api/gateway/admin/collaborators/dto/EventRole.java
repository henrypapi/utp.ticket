package com.pe.limon.api.gateway.admin.collaborators.dto;

import java.util.List;

public record EventRole (
        Long eventId,
        List<Long> roleIds
){
}
