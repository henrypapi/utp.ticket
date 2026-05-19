package com.pe.limon.api.gateway.client.access;


import com.pe.limon.api.transactions.tickets.bussiness.ControlAccessService;
import com.pe.limon.api.transactions.tickets.bussiness.QrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/client/access/passes")
@RequiredArgsConstructor
@Slf4j
public class ClientAccessController {
    private final ControlAccessService controlAccessService;
    private final QrService qrService;

    @GetMapping("/{accessId}")
    public ResponseEntity<?> getAccessPass(
            @PathVariable Long accessId,
            @RequestAttribute String userId) {
        return ResponseEntity.ok(controlAccessService.getById(userId, accessId));
    }

    @GetMapping("/image/{accessId}")
    public ResponseEntity<FileSystemResource> getImage(
            @PathVariable Long accessId,
            @RequestAttribute String userId) {
        FileSystemResource fileBytes = qrService.getFileQr(userId,accessId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(fileBytes);
    }
}
