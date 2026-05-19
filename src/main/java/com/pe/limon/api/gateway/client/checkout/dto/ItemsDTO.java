package com.pe.limon.api.gateway.client.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ItemsDTO {

        @NotNull(message = "El id es obligatorio")
        private Long ticketTypeId;

        @Valid
        @NotNull(message = "La assignedUsersId es obligatoria")
        @Size(min = 1)
        private List<
                @NotNull(message = "El userId no puede ser null")
                @NotBlank(message = "El userId no puede estar vacío")
                String> assignedUsersId;

}
