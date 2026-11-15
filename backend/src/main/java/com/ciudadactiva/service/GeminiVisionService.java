package com.ciudadactiva.service;

import com.ciudadactiva.dto.reporte.GeminiVisionAnalysis;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Inteligencia Artificial con Google Gemini Vision.
 * Realiza autotriage y diagnóstico preliminar de fotos de incidencias urbanas
 * (huecos/baches, basuras, luminarias, alcantarillas, etc.).
 */
@Service
public class GeminiVisionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionService.class);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Value("${app.gemini.api-key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analiza los bytes de una fotografía tomada en Bogotá para clasificar y diagnosticar la incidencia.
     */
    public GeminiVisionAnalysis analizarFotoReporte(byte[] fotoBytes, String mimeType) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.info("Gemini Vision omitido: GEMINI_API_KEY no configurada. Usando categorización por defecto.");
            return GeminiVisionAnalysis.noDisponible("IA en espera: Configure GEMINI_API_KEY para autotriage.");
        }

        if (fotoBytes == null || fotoBytes.length == 0) {
            return GeminiVisionAnalysis.noDisponible("Sin evidencia fotográfica para analizar.");
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(fotoBytes);
            String tipo = (mimeType != null && !mimeType.isBlank()) ? mimeType : "image/jpeg";

            String prompt = """
                Eres el perito técnico de infraestructura urbana de la Alcaldía de Bogotá (CiudadActiva).
                Analiza esta fotografía enviada por un ciudadano.
                Determina:
                1. 'esValido': true si muestra un daño en vía pública, andén, basura acumulada, alcantarilla, semáforo o luminaria; false si es una foto irrelevante o spam.
                2. 'categoriaDetectada': una de: 'HUECO_BACHE', 'BASURA_ACUMULADA', 'ALCANTARILLADO', 'LUMINARIA', 'SENALIZACION', 'OTRO'.
                3. 'prioridadSugerida': 'BAJA', 'MEDIA', 'ALTA' o 'CRITICA'.
                4. 'diagnostico': Resumen técnico formal de máximo 2 oraciones describiendo la afectación y riesgo peatonal/vehicular.
                
                IMPORTANTE: Responde ÚNICAMENTE un objeto JSON válido sin bloques markdown, sin ```json ni texto adicional:
                {"esValido": true, "categoriaDetectada": "...", "prioridadSugerida": "...", "diagnostico": "..."}
                """;

            Map<String, Object> partText = Map.of("text", prompt);
            Map<String, Object> inlineData = Map.of(
                    "mime_type", tipo,
                    "data", base64Image
            );
            Map<String, Object> partImage = Map.of("inline_data", inlineData);

            Map<String, Object> content = Map.of("parts", List.of(partText, partImage));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + "?key=" + geminiApiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidateTextNode = root.at("/candidates/0/content/parts/0/text");
                if (!candidateTextNode.isMissingNode()) {
                    String rawText = candidateTextNode.asText().trim();
                    // Limpieza en caso de que el modelo devuelva formato markdown
                    if (rawText.startsWith("```json")) {
                        rawText = rawText.replace("```json", "").replace("```", "").trim();
                    } else if (rawText.startsWith("```")) {
                        rawText = rawText.replace("```", "").trim();
                    }

                    JsonNode analysisJson = objectMapper.readTree(rawText);
                    boolean esValido = analysisJson.path("esValido").asBoolean(true);
                    String categoria = analysisJson.path("categoriaDetectada").asText("HUECO_BACHE");
                    String prioridad = analysisJson.path("prioridadSugerida").asText("MEDIA");
                    String diagnostico = analysisJson.path("diagnostico").asText("Incidencia validada por IA.");

                    log.info("Gemini Vision autotriage exitoso: categoria={}, prioridad={}", categoria, prioridad);
                    return new GeminiVisionAnalysis(esValido, categoria, prioridad, diagnostico);
                }
            } else {
                log.warn("Gemini Vision retornó código HTTP {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Excepción al ejecutar autotriage con Gemini Vision: {}", e.getMessage());
        }

        return GeminiVisionAnalysis.noDisponible("Evaluación automática completada con parámetros estándar.");
    }
}
