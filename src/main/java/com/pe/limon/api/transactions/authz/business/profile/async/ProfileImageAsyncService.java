package com.pe.limon.api.transactions.authz.business.profile.async;

import com.pe.limon.api.core.utils.file.CustomMultipartFile;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.core.utils.file.ImagesResizeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class ProfileImageAsyncService {

    @Value("${application.images.directory.user}")
    private String imageUploadPath;

    @Async("imageExecutor")
    public void process(String fileName,byte[] imageBytes) {
        try {
            MultipartFile image = new CustomMultipartFile(
                    "profileImage", "upload.jpg", "image/jpeg", imageBytes
            );

            if (!FileUtil.isValidFile(image, FileUtil.ALLOWED_EXTENSIONS_IMAGE)) return;

            MultipartFile resizedImage = ImagesResizeUtil.resizeAsMultipart(
                    image, 300, 300, "jpg", ImagesResizeUtil.Mode.COVER, 0.9f
            );

            FileUtil.saveFile(resizedImage, fileName, imageUploadPath);

        } catch (IOException e) {
            log.error("[process] Error while uploading profile image ", e);
        }
    }
}
