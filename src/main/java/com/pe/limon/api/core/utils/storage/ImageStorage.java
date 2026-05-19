package com.pe.limon.api.core.utils.storage;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ImageStorage implements IFileStorage {

    @Value("${application.images.allowed-mime-types}")
    private List<String> ALLOWED_MIME_TYPES;

    @Value("${application.images.max-file-size}")
    private long MAX_FILE_SIZE;

    @Value("${application.images.dangerous-extensions}")
    private List<String> DANGEROUS_EXTENSIONS;

    @Value("${application.images.invalidate-patterns}")
    private String INVALID_FILE_NAME_PATTERN;

    @Override
    public String saveFile(MultipartFile file, String fileName,String directory) throws IOException {
        log.info("saveFile starting {}", fileName);
        Path uploadPath = Paths.get(directory);

        if (!Files.exists(uploadPath)) throw new IOException(directory);

        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());
        log.info("saveFile finishing {}", filePath);
        return filePath.toString();
    }

    @Override
    public Resource getFile(String fileName,String directory) throws MalformedURLException {
        Path filePath = Paths.get(directory).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) return resource;
        else throw new RuntimeException("File not found or not readable: " + fileName);
    }


    @Override
    public boolean isValidFile(MultipartFile file, List<String> allowedExtensions) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        Pattern pattern = Pattern.compile(INVALID_FILE_NAME_PATTERN);
        if (
                file.getSize() > MAX_FILE_SIZE
                        || pattern.matcher(Objects.requireNonNull(file.getOriginalFilename())).find()
                        || !allowedExtensions.contains(fileExtension)
                        || DANGEROUS_EXTENSIONS.contains(fileExtension)
        ) return false;

        String mimeType = file.getContentType();
        return mimeType != null && ALLOWED_MIME_TYPES.contains(mimeType);
    }

    /**
     * Obtiene la extensión del archivo.
     * @param fileName Nombre del archivo.
     * @return Extensión del archivo.
     */
    private static String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf('.') > 0) return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return "";
    }
}
