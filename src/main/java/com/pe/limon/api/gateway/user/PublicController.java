package com.pe.limon.api.gateway.user;

import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.transactions.authz.business.profile.ProfileService;
import com.pe.limon.api.transactions.authz.business.user.UsersReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/public/users")
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    @Value("${application.images.directory.user}")
    private String pathUserImages;
    private final ProfileService profileService;
    private final UsersReadService usersReadService;

    @GetMapping("/profile/image/{filename}")
    public ResponseEntity<FileSystemResource> getProfileImage(@PathVariable String filename) {
        File file = FileUtil.getFileFromDirectory(pathUserImages, filename);

        MediaType contentType = getMediaTypeFromExtension(file.getName());

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/exists")
    public ResponseEntity<?> exists(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email
    ) {

        if (username != null && !username.isEmpty()) {
            String keySearch = username;
            if (email != null && !email.isEmpty()) keySearch = email.toLowerCase();
            return ResponseEntity.ok(usersReadService.getUser(keySearch));
        }
        return ResponseEntity.badRequest().body(new MessageDTO("Pararametros requeridos"));
    }

    private MediaType getMediaTypeFromExtension(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.valueOf("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
