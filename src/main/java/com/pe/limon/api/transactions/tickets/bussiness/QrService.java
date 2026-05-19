package com.pe.limon.api.transactions.tickets.bussiness;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.pe.limon.api.core.utils.codes.GenerateCodes;
import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.core.utils.security.SignatureUtil;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.file.FileUtil;

import com.pe.limon.api.transactions.tickets.repository.ControlAccessRepository;
import com.pe.limon.api.transactions.tickets.repository.entity.AccessPassEntity;
import com.pe.limon.api.transactions.tickets.repository.TicketRepository;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrService {

    private final TicketRepository ticketRepository;
    private final ControlAccessRepository controlAccessRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SignatureUtil signatureUtil;

    @Value("${application.images.directory.qr}")
    private String QR_DIRECTORY;

    public void registerAccessPass(List<AccessPassEntity> accessPass) {
        var results = controlAccessRepository.batchInsert(accessPass);

        if (results.length != accessPass.size()) {
            log.error("Resultado inconsistente: inserts={}, entities={}", results.length, accessPass.size());
            throw new BusinessException(
                    "Resultado inconsistente: inserts=" + results.length +
                            ", entities=" + accessPass.size()
            );
        }

        for (int i = 0; i < results.length; i++) {
            int r = results[i];

            if (r == 0) {
                log.error("No se insertó el item en la posición {}", i);
                throw new BusinessException(
                        "No se insertó el item en la posición " + i
                );
            }

            if (r != 1 && r != Statement.SUCCESS_NO_INFO) {
                log.error("Resultado inesperado en batch index {}: {}", i, r);
                throw new BusinessException(
                        "Resultado inesperado en batch index " + i + ": " + r
                );
            }
        }

    }

    public void generateAccessCode(Long ticketId, boolean generateQR) throws IOException, WriterException {
        var accessList = controlAccessRepository.findByTicketId(ticketId);
        log.info("[generateAccessCode] accessList {}",accessList);
        for (var access : accessList){
            if (generateQR) access = getWithContent(access);

            access.setStatus(StatusEnum.ACTIVE.getCode());
            int rows = controlAccessRepository.updateCodeAndStatusByTicketId(access);
            if (rows <= 0) throw new BusinessException("No hay access_pass para ticketId=" + access.getTicketId());
        }
    }

    public AccessPassEntity getWithContent(AccessPassEntity access) throws WriterException, IOException {
        String token = UUID.randomUUID().toString().substring(0,15)+ GenerateCodes.randomString(16);
        String signature = signatureUtil.sign(token);
        String content = "code="+token+"&sig="+signature;
        access.setCode(token);
        access.setStatus(StatusEnum.ACTIVE.getCode());

        BufferedImage qrImage = generateQrCode(content);
        saveQrImage(qrImage, makeFilename(String.valueOf(access.getId())));
        return access;
    }

    public FileSystemResource getFileQr(String userId, Long accessId) {
        Optional<AccessPassEntity> eTicketOptional = ticketRepository.findByUserId(userId, accessId);

        if (eTicketOptional.isEmpty() ||
            !eTicketOptional.get().getStatus().equals(StatusEnum.ACTIVE.getCode())
        ) throw new BusinessException("Ticket not found.");

        AccessPassEntity ticket = eTicketOptional.get();

        File file = FileUtil.getFileFromDirectory(QR_DIRECTORY, makeFilename(String.valueOf(ticket.getId())));

        if (!file.exists() || !file.isFile()) throw new BusinessException("File not found.");
        return new FileSystemResource(file);
    }

    public BufferedImage generateQrCode(String content) throws WriterException {
        BitMatrix matrix = new MultiFormatWriter().encode(
                content, BarcodeFormat.QR_CODE, 300, 300);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    private String makeFilename(String random){
        return "ticket-" + random +".png";
    }

    public void saveQrImage(BufferedImage image, String fileName) throws IOException {
        File dir = new File(QR_DIRECTORY);
        if (!dir.exists()) dir.mkdirs();

        File outputFile = new File(dir, fileName);
        ImageIO.write(image, "png", outputFile);
    }
}
