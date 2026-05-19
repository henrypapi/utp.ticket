package com.pe.limon.api.gateway.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class UserPassDTO{
    @NotNull(message = "{error.username.notnull}")
    @NotBlank(message = "{error.username.blank}")
    String username;

    @NotNull(message = "{error.password.notnull}")
    @NotBlank(message = "{error.password.blank}")
    String password;
}

