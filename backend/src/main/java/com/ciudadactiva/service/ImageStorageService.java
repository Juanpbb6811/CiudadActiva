package com.ciudadactiva.service;

import com.ciudadactiva.dto.reporte.FotoData;
import com.ciudadactiva.entity.Reporte;
import com.ciudadactiva.entity.ReporteImagen;
import com.ciudadactiva.repository.ReporteImagenRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de Almacenamiento Segregado de Imágenes (Arquitectura Multi-Database).
 * Permite desviar el almacenamiento binario BYTEA a una base de datos secundaria
 * dedicada exclusivamente a imágenes pesadas (ej: Neon, PostgreSQL dedicado o réplica),
 * protegiendo la base de datos principal de agotamiento de espacio.
 */
@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    private final ReporteImagenRepository localImagenRepository;

    @Value("${spring.image-datasource.url:${SPRING_IMAGE_DATASOURCE_URL:}}")
    private String imageDbUrl;

    @Value("${spring.image-datasource.username:${SPRING_IMAGE_DATASOURCE_USERNAME:}}")
    private String imageDbUser;

    @Value("${spring.image-datasource.password:${SPRING_IMAGE_DATASOURCE_PASSWORD:}}")
    private String imageDbPassword;

    private HikariDataSource secondaryDataSource;

    public ImageStorageService(ReporteImagenRepository localImagenRepository) {
        this.localImagenRepository = localImagenRepository;
    }

    @PostConstruct
    public void init() {
        if (imageDbUrl != null && !imageDbUrl.isBlank()) {
            try {
                log.info("🔌 Configurando base de datos secundaria EXCLUSIVA para imágenes en: {}", imageDbUrl);
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(imageDbUrl);
                config.setUsername(imageDbUser != null ? imageDbUser : "postgres");
                config.setPassword(imageDbPassword != null ? imageDbPassword : "");
                config.setMaximumPoolSize(4);
                config.setMinimumIdle(1);
                config.setPoolName("DedicatedImageDbPool");

                this.secondaryDataSource = new HikariDataSource(config);
                crearTablaImagenesSecundariaSiNoExiste();
                log.info("✅ Conexión exitosa a la base de datos exclusiva para imágenes.");
            } catch (Exception e) {
                log.error("⚠️ No se pudo inicializar la base de datos secundaria de imágenes: {}. Se usará la base de datos principal.", e.getMessage());
                this.secondaryDataSource = null;
            }
        } else {
            log.info("ℹ️ Sin base de datos secundaria configurada. Las imágenes se almacenan en la tabla dedicada 'reporte_imagenes' de la base de datos principal.");
        }
    }

    @PreDestroy
    public void close() {
        if (secondaryDataSource != null && !secondaryDataSource.isClosed()) {
            secondaryDataSource.close();
        }
    }

    /**
     * Guarda la imagen binaria optimizada en la base de datos exclusiva (o local como fallback).
     */
    public void guardarImagen(Reporte reporte, byte[] bytes, String tipoMime, String nombreArchivo) {
        if (secondaryDataSource != null) {
            String sql = "INSERT INTO reporte_imagenes_store (reporte_id, datos_binarios, tipo_mime, nombre_archivo, peso_bytes, creado_en) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = secondaryDataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, reporte.getId());
                ps.setBytes(2, bytes);
                ps.setString(3, tipoMime != null ? tipoMime : "image/jpeg");
                ps.setString(4, nombreArchivo != null ? nombreArchivo : "evidencia.jpg");
                ps.setLong(5, bytes.length);
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                log.info("🖼️ Imagen de reporte #{} guardada en la base de datos EXCLUSIVA de imágenes ({} bytes)", reporte.getId(), bytes.length);
                return;
            } catch (Exception e) {
                log.error("Error al guardar en base de datos secundaria de imágenes: {}. Guardando en tabla principal.", e.getMessage());
            }
        }

        // Almacenamiento en tabla dedicada principal
        ReporteImagen img = new ReporteImagen(reporte, bytes, tipoMime, nombreArchivo);
        localImagenRepository.save(img);
        log.info("🖼️ Imagen de reporte #{} guardada en la tabla dedicada 'reporte_imagenes' ({} bytes)", reporte.getId(), bytes.length);
    }

    /**
     * Recupera la imagen binaria desde la base de datos secundaria o principal.
     */
    public Optional<FotoData> obtenerImagen(Long reporteId) {
        if (secondaryDataSource != null) {
            String sql = "SELECT datos_binarios, tipo_mime FROM reporte_imagenes_store WHERE reporte_id = ? ORDER BY id DESC LIMIT 1";
            try (Connection conn = secondaryDataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, reporteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        byte[] bytes = rs.getBytes("datos_binarios");
                        String mime = rs.getString("tipo_mime");
                        return Optional.of(new FotoData(bytes, mime));
                    }
                }
            } catch (Exception e) {
                log.warn("Fallo al leer de base de datos secundaria de imágenes: {}. Probando en almacén local.", e.getMessage());
            }
        }

        return localImagenRepository.findFirstByReporteIdOrderByIdDesc(reporteId)
                .map(img -> new FotoData(img.getDatosBinarios(), img.getTipoMime()));
    }

    private void crearTablaImagenesSecundariaSiNoExiste() {
        String ddl = """
            CREATE TABLE IF NOT EXISTS reporte_imagenes_store (
                id BIGSERIAL PRIMARY KEY,
                reporte_id BIGINT NOT NULL,
                datos_binarios BYTEA NOT NULL,
                tipo_mime VARCHAR(50) NOT NULL,
                nombre_archivo VARCHAR(255),
                peso_bytes BIGINT,
                creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            CREATE INDEX IF NOT EXISTS idx_imgstore_reporte_id ON reporte_imagenes_store(reporte_id);
            """;
        try (Connection conn = secondaryDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.execute();
            log.info("Estructura de tabla 'reporte_imagenes_store' verificada en la base de datos exclusiva de imágenes.");
        } catch (Exception e) {
            log.warn("Aviso al verificar tabla en base de datos secundaria: {}", e.getMessage());
        }
    }
}
