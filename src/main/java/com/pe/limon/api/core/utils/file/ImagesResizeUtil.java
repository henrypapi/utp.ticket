package com.pe.limon.api.core.utils.file;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Iterator;

public final class ImagesResizeUtil {

    public enum Mode { FIT, COVER }

    private ImagesResizeUtil() {}

    /* ===================== API PRINCIPAL ===================== */

    public static MultipartFile resizeAsMultipart(MultipartFile file,
                                                  int width, int height,
                                                  String outputFormat,
                                                  Mode mode,
                                                  float qualityIfJpg) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("El MultipartFile de entrada está vacío.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = file.getInputStream()) {
            resize(in, baos, width, height, outputFormat, mode, qualityIfJpg);
        }

        byte[] bytes = baos.toByteArray();
        String originalName = file.getOriginalFilename();
        String baseName = (originalName == null) ? "image" : stripExtension(originalName);
        String ext = normalizeExt(outputFormat);
        String newFilename = baseName + "_" + width + "x" + height + "." + ext;
        String contentType = ext.equals("png") ? "image/png" : "image/jpeg";

        // ⚡ Usa nuestra implementación interna
        return new CustomMultipartFile(
                file.getName(),
                newFilename,
                contentType,
                bytes
        );
    }

    public static void resize(InputStream in, OutputStream out,
                              int width, int height,
                              String outputFormat, Mode mode,
                              float qualityIfJpg) throws IOException {
        BufferedImage src = ImageIO.read(in);
        if (src == null) throw new IOException("No se pudo leer la imagen de entrada.");

        BufferedImage dst = resizeBuffered(src, width, height, mode);

        // 🔴 Solo reducción de pixeles + conversión a formato simple
        writeSimple(dst, outputFormat, out);
    }

    private static void writeSimple(BufferedImage img, String format, OutputStream out) throws IOException {
        String fmt = (format == null) ? "jpg" : format.trim().toLowerCase();
        if (!fmt.equals("jpg") && !fmt.equals("jpeg") && !fmt.equals("png")) {
            throw new IllegalArgumentException("Formato no soportado: " + format + " (usa jpg|png)");
        }

        if (fmt.equals("png")) {
            // Para PNG no hace falta conversión, solo escribimos
            ImageIO.write(img, "png", out);
            return;
        }

        // Para JPG: imagen limpia, RGB sin alpha ni espacios raros
        BufferedImage rgb = toRgbSimple(img);
        ImageIO.write(rgb, "jpg", out);
    }

    /** Devuelve la imagen redimensionada en memoria. */
    public static BufferedImage resize(BufferedImage src,
                                       int width, int height,
                                       Mode mode) {
        return resizeBuffered(src, width, height, mode);
    }

    /* ===================== NÚCLEO DE REDIMENSIÓN ===================== */

    private static BufferedImage resizeBuffered(BufferedImage src, int targetW, int targetH, Mode mode) {
        if (targetW <= 0 || targetH <= 0) throw new IllegalArgumentException("width/height deben ser > 0");

        int srcW = src.getWidth();
        int srcH = src.getHeight();

        double scaleX = targetW / (double) srcW;
        double scaleY = targetH / (double) srcH;

        double scale;
        int drawW, drawH, offsetX = 0, offsetY = 0;

        if (mode == Mode.FIT) {
            // Encaja dentro del cuadro manteniendo proporción, puede quedar “barras” (transparente para PNG, negro para JPG).
            scale = Math.min(scaleX, scaleY);
            drawW = (int) Math.round(srcW * scale);
            drawH = (int) Math.round(srcH * scale);
            offsetX = (targetW - drawW) / 2;
            offsetY = (targetH - drawH) / 2;
        } else { // COVER
            // Cubre todo el cuadro: recorta excedente manteniendo proporción.
            scale = Math.max(scaleX, scaleY);
            drawW = (int) Math.round(srcW * scale);
            drawH = (int) Math.round(srcH * scale);
            offsetX = (targetW - drawW) / 2;
            offsetY = (targetH - drawH) / 2;
        }

        int type = src.getType();
        if (type == 0) type = BufferedImage.TYPE_INT_ARGB; // por defecto con alfa

        // Si quieres fondo opaco para JPG en FIT con barras: usa TYPE_INT_RGB (sin alfa).
        boolean wantsAlphaCanvas = type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_4BYTE_ABGR;
        BufferedImage dst = new BufferedImage(targetW, targetH,
                wantsAlphaCanvas ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        Graphics2D g = dst.createGraphics();
        try {
            // Fondo: transparente si ARGB, negro si RGB (puedes cambiar a blanco).
            if (dst.getTransparency() == Transparency.OPAQUE) {
                g.setColor(Color.BLACK); // cambia a Color.WHITE si prefieres
                g.fillRect(0, 0, targetW, targetH);
            } else {
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, targetW, targetH);
                g.setComposite(AlphaComposite.SrcOver);
            }

            // Calidad alta
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            AffineTransform at = AffineTransform.getTranslateInstance(offsetX, offsetY);
            at.scale(scale, scale);
            g.drawRenderedImage(src, at);
        } finally {
            g.dispose();
        }

        // En COVER, recorte ya queda centrado por offsets negativos/positivos. El canvas es exactamente targetW x targetH.
        return dst;
    }

    private static void write(BufferedImage img, String format, OutputStream out, float qualityIfJpg) throws IOException {
        String fmt = (format == null) ? "jpg" : format.trim().toLowerCase();
        if (!fmt.equals("jpg") && !fmt.equals("jpeg") && !fmt.equals("png")) {
            throw new IllegalArgumentException("Formato no soportado: " + format + " (usa jpg|png)");
        }

        if (fmt.equals("png")) {
            // PNG sin pérdidas
            ImageIO.write(img, "png", out);
            return;
        }

        // JPG con compresión controlada
        float q = clamp01(qualityIfJpg <= 0 ? 0.85f : qualityIfJpg);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            // Fallback simple (sin control de quality)
            ImageIO.write(img, "jpg", out);
            return;
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(q); // 0.0 = máx compresión (peor calidad), 1.0 = sin pérdidas (aprox)
            }
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static String stripExtension(String filename) {
        String name = Paths.get(filename).getFileName().toString();
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    private static String normalizeExt(String fmt) {
        if (fmt == null) return "jpg";
        String f = fmt.trim().toLowerCase();
        return (f.equals("jpeg")) ? "jpg" : (f.equals("png") ? "png" : "jpg");
    }

    private static BufferedImage toRgbSimple(BufferedImage src) {
        // Si ya es RGB "normal", no hacemos nada
        if (!src.getColorModel().hasAlpha()
                && (src.getType() == BufferedImage.TYPE_INT_RGB
                || src.getType() == BufferedImage.TYPE_3BYTE_BGR)) {
            return src;
        }

        BufferedImage rgb = new BufferedImage(
                src.getWidth(),
                src.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = rgb.createGraphics();
        try {
            g.setColor(Color.WHITE); // fondo blanco si la original tenía transparencia
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }

        return rgb;
    }


}