package com.pe.limon.api.transactions.authz.business.user;

import lombok.Getter;

public enum UserErrorEnum {
    // Definición de los errores con código y descripción
    USER_NOT_VERIFIED("ERR001", "El usuario no está verificado."),
    USER_NOT_FOUND("ERR002", "Usuario no exsite."),
    DATA_DUPLICATED("ERR003", "El número de telefonó, email o número de documento ya se encuentran registrados."),
    CSV_UPLOAD_ERR("ERR004", "Hubo un error al cargar el csv."),
    NUM_PENDENT_VERIFICATION("ERR004", "Este número ya se encuentra verificado."),
    NUM_ALREADY_VERIFIED("ERR004", "Este número está pendiente de verificación."),
    SEND_VERIFICATION_ERR("ERR004", "No se pudo realizar el envío de código de verificación."),
    UNKNOWN_ERROR("ERR999", "Error desconocido.");

    // Campos para el código y la descripción del error
    private final String code;
    @Getter
    private final String value;

    // Constructor
    UserErrorEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    // Métodos para acceder a los valores
    public String getCode() {
        return value;
    }

    @Override
    public String toString() {
        return code + ": " + value;
    }
}

