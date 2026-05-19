package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PersonalInfoEntity {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String province;
    private String district;
    private String address;
    private String documentType;
    private String documentNumber;
    private LocalDate birthDate;
    private String gender;
    private String phoneNumber;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
    private LocalDateTime updatedDatetime;
    private Long updatedTimestamp;
}
