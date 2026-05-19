package com.pe.limon.api.core.utils.generics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResponseWrapDTO<T> {
    private int code;
    private String message;
    private T data;


}