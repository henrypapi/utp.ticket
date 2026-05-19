package com.pe.limon.api.core.utils.file;

import com.pe.limon.api.core.utils.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
public class FileUtil {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "application/csv");
    public static final List<String> ALLOWED_EXTENSIONS_IMAGE = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList("exe", "sh", "bat", "cmd", "js", "vbs");
    private static final Pattern INVALID_FILE_NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9\\-_.]");
    public static final List<String> ALLOWED_EXTENSIONS_FILE = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf");

    /**
     * Verifica si el archivo es válido según las extensiones permitidas.
     * @param file El archivo a verificar.
     * @param allowedExtensions Lista de extensiones permitidas.
     * @return true si el archivo es válido, false si no lo es.
     */
    public static boolean isValidFile(MultipartFile file, List<String> allowedExtensions) {

        String fileExtension = getFileExtension(file.getOriginalFilename());
        log.info("[isValidFile] File extension: {}", !allowedExtensions.contains(fileExtension));
        log.info("[isValidFile] Size: {}", file.getSize() > MAX_FILE_SIZE);
        log.info("[isValidFile] Dangerous extensions: {}", DANGEROUS_EXTENSIONS.contains(fileExtension));
        log.info("[isValidFile] File Name: {}", INVALID_FILE_NAME_PATTERN.matcher(Objects.requireNonNull(file.getOriginalFilename())).find());
        if (
            file.getSize() > MAX_FILE_SIZE
            //|| INVALID_FILE_NAME_PATTERN.matcher(Objects.requireNonNull(file.getOriginalFilename())).find()
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
    public static String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf('.') > 0) return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        return "";
    }

    /**
     * Guarda el archivo en el directorio especificado.
     * @param file El archivo que se va a guardar.
     * @param directory El directorio en el que se guardará el archivo.
     * @return El archivo guardado.
     */
    public static File saveFile(MultipartFile file, String fileName,String directory) throws IOException {
        log.info("[saveFile] Starting: {}-{}", directory,fileName);

        File destinationFile = new File(directory + File.separator + fileName);
        file.transferTo(destinationFile);
        log.info("[saveFile] End: {}-{}", directory,fileName);

        return destinationFile;
    }

    /**
     * Obtiene un archivo del directorio especificado.
     * @param directory El directorio de donde obtener el archivo.
     * @param fileName El nombre del archivo.
     * @return El archivo obtenido.
     */
    public static File getFileFromDirectory(String directory, String fileName) {
        log.debug("[getFileFromDirectory] File Path : {}",directory + File.separator + fileName);
        return new File(directory + File.separator + fileName);
    }

    // Detecta el tipo MIME del archivo según su contenido
    public static MediaType detectMediaType(File file) {
        try {
            String mimeType = Files.probeContentType(file.toPath());
            return (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    // Valida el nombre del archivo para evitar rutas peligrosas o extensiones maliciosas
    public static void validateFileName(String fileName) {
        String extension = getFileExtension(fileName);
        if (DANGEROUS_EXTENSIONS.contains(extension) || INVALID_FILE_NAME_PATTERN.matcher(fileName).find()) {
            throw new BusinessException("Nombre de archivo no permitido.");
        }
    }

    public static File findImageFileWithAnyExtension(String directory, String baseName) {
        String[] possibleExtensions = { "png", "jpg", "jpeg", "webp" };
        for (String ext : possibleExtensions) {
            File file = new File(directory + File.separator + baseName + "." + ext);
            if (file.exists() && file.isFile()) {
                return file;
            }
        }
        return null;
    }
}