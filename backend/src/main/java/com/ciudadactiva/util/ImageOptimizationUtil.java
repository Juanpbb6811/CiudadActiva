package com.ciudadactiva.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@Component
public class ImageOptimizationUtil {

    private static final Logger log = LoggerFactory.getLogger(ImageOptimizationUtil.class);
    private static final int MAX_DIMENSION = 1200;
    private static final float COMPRESSION_QUALITY = 0.80f;

    public record OptimizedImage(byte[] bytes, String mimeType, int pesoBytes) {}

    public OptimizedImage optimizar(byte[] rawBytes, String originalMime) {
        if (rawBytes == null || rawBytes.length == 0) {
            return new OptimizedImage(new byte[0], "image/jpeg", 0);
        }

        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawBytes));
            if (original == null) {
                // Si ImageIO no lo decodifica (ej: vector SVG o formato especial), conservar intacto
                return new OptimizedImage(rawBytes, originalMime != null ? originalMime : "image/jpeg", rawBytes.length);
            }

            int ancho = original.getWidth();
            int alto = original.getHeight();

            // Redimensionar si supera la dimensión máxima
            if (ancho > MAX_DIMENSION || alto > MAX_DIMENSION) {
                double scale = Math.min((double) MAX_DIMENSION / ancho, (double) MAX_DIMENSION / alto);
                ancho = (int) (ancho * scale);
                alto = (int) (alto * scale);
            }

            BufferedImage redimensionada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = redimensionada.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(original, 0, 0, ancho, alto, Color.WHITE, null);
            g2d.dispose();

            // Comprimir a JPEG con calidad 80%
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                    writer.setOutput(ios);
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    if (param.canWriteCompressed()) {
                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        param.setCompressionQuality(COMPRESSION_QUALITY);
                    }
                    writer.write(null, new IIOImage(redimensionada, null, null), param);
                } finally {
                    writer.dispose();
                }
            } else {
                ImageIO.write(redimensionada, "jpg", baos);
            }

            byte[] optimizados = baos.toByteArray();
            log.info("🖼️ Imagen optimizada: {} KB -> {} KB ({}x{} px)",
                    rawBytes.length / 1024, optimizados.length / 1024, ancho, alto);

            return new OptimizedImage(optimizados, "image/jpeg", optimizados.length);

        } catch (Exception e) {
            log.warn("No se pudo comprimir la imagen, almacenando original: {}", e.getMessage());
            return new OptimizedImage(rawBytes, originalMime != null ? originalMime : "image/jpeg", rawBytes.length);
        }
    }
}
