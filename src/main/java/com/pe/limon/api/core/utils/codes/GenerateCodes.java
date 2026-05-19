package com.pe.limon.api.core.utils.codes;

import java.security.SecureRandom;

public class GenerateCodes {

    private static final String CHARACTERS = "0123456789";
    private static final String CHARACTERS_ID = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMBERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String randomString(int length) {
        return createRandomString(length,CHARACTERS);
    }

    public static String randomStringId(int length) {
        return createRandomString(length,CHARACTERS_ID);
    }

    public static long randomNumber(int length) {
        return Integer.parseInt(createRandomString(length,NUMBERS));
    }

    private static String createRandomString(int length, String keys){
        StringBuilder randomValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(keys.length());
            randomValue.append(keys.charAt(index));
        }
        return randomValue.toString();
    }

}
