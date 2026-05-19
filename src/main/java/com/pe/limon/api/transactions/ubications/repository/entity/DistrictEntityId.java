package com.pe.limon.api.transactions.ubications.repository.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class DistrictEntityId implements Serializable {
    private String codDist;
    private String codProv;
    private String codDepa;
}