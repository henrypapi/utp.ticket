package com.pe.limon.api.core.aop;

public record EventUserRequest (
        String userId,
        Long eventId
){

}
