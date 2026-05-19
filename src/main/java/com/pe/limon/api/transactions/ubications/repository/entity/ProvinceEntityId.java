package com.pe.limon.api.transactions.ubications.repository.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProvinceEntityId implements Serializable {
    private String codProv;
    private String codDepa;
}