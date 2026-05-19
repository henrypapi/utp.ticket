package com.pe.limon.api.core.utils.conversor;

public class FormatUtils {

    public static String splitEmail(String email){
        String[] parts = email.split("@");
        if (parts.length > 0) {
            return parts[0];
        } else {
            throw new IllegalArgumentException("email inválido");
        }
    }
}
