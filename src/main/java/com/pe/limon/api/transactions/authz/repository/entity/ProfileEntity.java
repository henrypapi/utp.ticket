package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProfileEntity {
    private String userId;
    private String username;
    private String displayName;
    private String bio;
    private String profileImage;
    private String coverImage;
    private String websiteUrl;
    private LocalDateTime registeredDateTime;
    private Long registeredTimestamp;
    private LocalDateTime updatedDateTime;
    private Long updatedTimestamp;
}
