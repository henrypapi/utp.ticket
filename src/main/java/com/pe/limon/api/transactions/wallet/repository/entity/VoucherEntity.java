package com.pe.limon.api.transactions.wallet.repository.entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VoucherEntity {
    private MultipartFile multipartFile;
    private String fileName;
}
