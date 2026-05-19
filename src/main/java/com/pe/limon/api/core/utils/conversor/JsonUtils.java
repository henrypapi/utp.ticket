package com.pe.limon.api.core.utils.conversor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    public static String convertToJsonString(Object objeto){
        try{return objectMapper.writeValueAsString(objeto);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static Map<String,Object> convertToMap(String json) {
        try{
            return objectMapper.readValue(json, Map.class);
        }catch (Exception e){
            return new HashMap<>();
        }

    }

    public static <T> T convertToObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        }catch (Exception e){
            return null;
        }
    }

    public static <T> T convertToObjectList(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
