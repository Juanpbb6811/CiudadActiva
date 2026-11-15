package com.ciudadactiva.service;

import com.ciudadactiva.dto.reporte.DireccionBogota;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Servicio de Georreferenciación Especializado para Bogotá D.C.
 * Integra la Infraestructura de Datos Espaciales del Distrito Capital (IDECA)
 * con fallback de alta disponibilidad a OpenStreetMap Nominatim.
 */
@Service
public class BogotaGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(BogotaGeocodingService.class);

    private static final String IDECA_REVERSE_URL = "https://serviciosgis.catastrobogota.gov.co/arcgis/rest/services/Geocodificacion/Geocodificador/GeocodeServer/reverseGeocode?f=json&location=%f,%f";
    private static final String NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%f&lon=%f&zoom=18&addressdetails=1";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Resuelve las coordenadas geográficas a una dirección y barrio oficial de Bogotá.
     */
    public DireccionBogota georreferenciarInversa(Double latitud, Double longitud, String direccionFallback) {
        if (latitud == null || longitud == null) {
            return DireccionBogota.porDefecto(direccionFallback);
        }

        // Intento 1: Catastro Distrital / IDECA Bogotá
        try {
            DireccionBogota idecaRes = consultarIdeca(latitud, longitud);
            if (idecaRes != null) {
                log.info("Georreferenciación oficial IDECA exitosa: {}", idecaRes.direccionFormateada());
                return idecaRes;
            }
        } catch (Exception e) {
            log.warn("IDECA no disponible temporalmente ({}), pasando a fallback Nominatim", e.getMessage());
        }

        // Intento 2: Nominatim OSM
        try {
            DireccionBogota nomRes = consultarNominatim(latitud, longitud);
            if (nomRes != null) {
                log.info("Georreferenciación Nominatim exitosa: {}", nomRes.direccionFormateada());
                return nomRes;
            }
        } catch (Exception e) {
            log.warn("Error en georreferenciación Nominatim: {}", e.getMessage());
        }

        return DireccionBogota.porDefecto(direccionFallback);
    }

    private DireccionBogota consultarIdeca(Double lat, Double lon) throws Exception {
        // En ArcGIS Server location se pasa como: x (lon), y (lat)
        String url = String.format(java.util.Locale.US, IDECA_REVERSE_URL, lon, lat);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "CiudadActiva-Bogota/1.0")
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode addressNode = root.path("address");
            if (!addressNode.isMissingNode()) {
                String matchAddr = addressNode.path("Match_addr").asText("");
                String barrio = addressNode.path("Neighborhood").asText("Bogotá");
                String localidad = addressNode.path("City").asText("Bogotá D.C.");

                if (!matchAddr.isBlank()) {
                    return new DireccionBogota(matchAddr, barrio, localidad, "Bogotá D.C.");
                }
            }
        }
        return null;
    }

    private DireccionBogota consultarNominatim(Double lat, Double lon) throws Exception {
        String url = String.format(java.util.Locale.US, NOMINATIM_REVERSE_URL, lat, lon);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "CiudadActiva-Bogota/1.0 (contacto: soporte@ciudadactiva.gov.co)")
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode address = root.path("address");

            String road = address.path("road").asText("");
            String houseNumber = address.path("house_number").asText("");
            String suburb = address.path("suburb").asText(address.path("neighbourhood").asText("Chapinero"));
            String cityDistrict = address.path("city_district").asText(address.path("city").asText("Bogotá"));

            String dirFormateada;
            if (!road.isBlank() && !houseNumber.isBlank()) {
                dirFormateada = road + " #" + houseNumber;
            } else if (!road.isBlank()) {
                dirFormateada = road;
            } else {
                dirFormateada = root.path("display_name").asText("Bogotá D.C.");
            }

            return new DireccionBogota(dirFormateada, suburb, cityDistrict, "Bogotá D.C.");
        }
        return null;
    }
}
