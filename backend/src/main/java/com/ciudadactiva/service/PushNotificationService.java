package com.ciudadactiva.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Servicio de Notificaciones Push con Firebase Cloud Messaging (FCM).
 * Emite alertas en tiempo real a los dispositivos móviles de los ciudadanos
 * cuando el estado de sus reportes urbanos cambia en terreno.
 */
@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);
    private static final String FCM_LEGACY_URL = "https://fcm.googleapis.com/fcm/send";

    @Value("${app.fcm.server-key:${FCM_SERVER_KEY:}}")
    private String fcmServerKey;

    @Autowired
    private EmailService emailService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Notifica al ciudadano sobre la actualización de su radicado en la ciudad.
     */
    public void notificarCambioEstado(
            Long reporteId,
            String codigoRadicado,
            String estadoAnterior,
            String estadoNuevo,
            String emailCiudadano,
            String nombreCiudadano,
            String observaciones
    ) {
        String tituloPush = "Actualización de tu reporte: " + codigoRadicado;
        String cuerpoPush = "Tu reporte ha cambiado a estado: " + estadoNuevo.replace("_", " ");
        if (observaciones != null && !observaciones.isBlank()) {
            cuerpoPush += " (" + observaciones + ")";
        }

        // 1. Envío a Firebase Cloud Messaging si la clave del servidor está configurada
        if (fcmServerKey != null && !fcmServerKey.isBlank()) {
            despacharFcm(reporteId, codigoRadicado, tituloPush, cuerpoPush);
        } else {
            log.info("FCM en espera: FCM_SERVER_KEY no configurada. Notificación push registrada en log para móvil: [{}] {}", tituloPush, cuerpoPush);
        }

        // 2. Notificación complementaria vía Correo Electrónico para alta disponibilidad
        if (emailCiudadano != null && !emailCiudadano.isBlank()) {
            emailService.enviarNotificacionEstadoReporte(
                    emailCiudadano,
                    nombreCiudadano != null ? nombreCiudadano : "Ciudadano",
                    codigoRadicado,
                    estadoAnterior,
                    estadoNuevo,
                    observaciones
            );
        }
    }

    private void despacharFcm(Long reporteId, String radicado, String titulo, String cuerpo) {
        try {
            Map<String, Object> notification = Map.of(
                    "title", titulo,
                    "body", cuerpo,
                    "sound", "default"
            );

            Map<String, Object> data = Map.of(
                    "reporteId", String.valueOf(reporteId),
                    "radicado", radicado,
                    "click_action", "OPEN_REPORTE_DETALLE"
            );

            // Despacho a topic del reporte para que los suscriptores reciban la alerta
            Map<String, Object> payload = Map.of(
                    "to", "/topics/reporte-" + reporteId,
                    "notification", notification,
                    "data", data,
                    "priority", "high"
            );

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FCM_LEGACY_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "key=" + fcmServerKey)
                    .timeout(Duration.ofSeconds(6))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("FCM push status code {}: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.error("Error al enviar notificación push vía FCM: {}", e.getMessage());
        }
    }
}
