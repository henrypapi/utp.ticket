package com.pe.limon.api.transactions.events.repository.event;

import com.pe.limon.api.core.utils.generics.GeoPoint;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventEntity {
    private Long id;
    private String name;
    private String status;
    private String description;
    private String typeId;
    private String paymentMode;
    private String img;
    private String address;
    private String location;
    private GeoPoint geoLocation;
    private String timeZone;
    private String country;
    private LocalDateTime startDatetime;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
    private Boolean qrEnabled;
    private String ownerUserId;
    private String terms;
    private UserEntity ownerUser;
    private String metadata;
    private String redirectAfterPay;
    private String paymentComment;
}
