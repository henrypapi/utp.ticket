package com.pe.limon.api.core.utils.enums;

public enum SocialLoginCode {
    GOOGLE(1,"google"),
    FACEBOOK(2,"facebook");

    private final int code;
    private final String name;

    SocialLoginCode(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static int fromName(String name) {
        for (SocialLoginCode social : SocialLoginCode.values()) {
            if (social.getName().equals(name)) return social.getCode();
        }
        throw new IllegalArgumentException("Proveedor no válido: " + name);
    }
}

