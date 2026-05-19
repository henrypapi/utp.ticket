package com.pe.limon.api.gateway.admin.collaborators.dto;

import java.util.List;

public record CollabDTO (
        Long collabId,
        String userId,
        List<String> roles
){
}
