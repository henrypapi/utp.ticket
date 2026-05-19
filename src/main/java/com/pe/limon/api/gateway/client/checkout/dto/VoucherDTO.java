package com.pe.limon.api.gateway.client.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class VoucherDTO {
        @NotNull
        private MultipartFile voucher;
        @NotNull
        private Long eventId;
        @Valid
        @NotNull
        @Size(min = 1)
        private List<ItemsDTO> items;
        private AttributionDTO attribution;
}

