package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@ToString
public class UserEntity {
    private String id;
    private String email;
    private String password;
    private boolean registrationCompleted;
    private boolean profileCompleted;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String phoneNumber;
    private Boolean verified;
    private Set<RoleEntity> roles;
    private String verificationCode;
    private String provider;
    private String documentNumber;
    private LocalDateTime verificationCodeExp;
    private String status;
    private Long registeredTimestamp;
    private LocalDateTime registeredDatetime;
    private Long deletedTimestamp;
    private LocalDateTime deletedDatetime;
    private PersonalInfoEntity personalInfo;
    private ProfileEntity profile;

}

