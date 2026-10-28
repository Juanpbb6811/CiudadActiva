package com.ciudadactiva.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Descarga de la Aplicación Móvil", description = "Endpoint público para descargar el archivo APK oficial de CiudadActiva")
public class AppDownloadController {

    @GetMapping(value = {"/descargar", "/api/app/descargar", "/app.apk"})
    @Operation(summary = "Descargar APK de CiudadActiva", description = "Descarga directa del APK oficial de CiudadActiva con tipos MIME de Android.")
    public ResponseEntity<Resource> descargarApk() {
        Resource apkResource = new ClassPathResource("static/app.apk");
        if (!apkResource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.android.package-archive")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"CiudadActiva-v0.2.0.apk\"")
                .body(apkResource);
    }
}
