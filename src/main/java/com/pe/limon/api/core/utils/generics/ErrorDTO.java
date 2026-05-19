package com.pe.limon.api.core.utils.generics;

import java.time.LocalDateTime;

public record ErrorDTO (
        Integer code,

        String message,

        LocalDateTime timeStamp)
{
        @Override
        public String toString() {
                return "ErrorDTO{" +
                        "code=" + code +
                        ", message='" + message + '\'' +
                        ", timeStamp=" + timeStamp +
                        '}';
        }
}
