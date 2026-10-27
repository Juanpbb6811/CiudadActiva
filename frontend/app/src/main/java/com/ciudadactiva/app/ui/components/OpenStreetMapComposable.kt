package com.ciudadactiva.app.ui.components

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ciudadactiva.app.data.model.ReporteResponse
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "LeafletMap"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OpenStreetMapComposable(
    modifier: Modifier = Modifier,
    centerLat: Double = 4.60971,
    centerLng: Double = -74.08175,
    zoom: Int = 14,
    userLocation: Pair<Double, Double>? = null,
    reportes: List<ReporteResponse> = emptyList(),
    isPickerMode: Boolean = false,
    selectedLat: Double? = null,
    selectedLng: Double? = null,
    onLocationSelected: ((Double, Double) -> Unit)? = null,
    onReportClick: ((Long) -> Unit)? = null
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Serializar reportes a JSON seguro para Leaflet
    val reportesJson = remember(reportes) {
        val jsonArray = JSONArray()
        reportes.forEach { rep ->
            if (rep.latitud != null && rep.longitud != null) {
                val obj = JSONObject().apply {
                    put("id", rep.id)
                    put("titulo", (rep.titulo ?: "Incidencia").replace("\"", "'").replace("\n", " "))
                    put("descripcion", (rep.descripcion ?: "").replace("\"", "'").replace("\n", " "))
                    put("categoria", (rep.categoriaNombre ?: "General").replace("\"", "'"))
                    put("direccion", (rep.direccion ?: "Sector urbano").replace("\"", "'"))
                    put("latitud", rep.latitud)
                    put("longitud", rep.longitud)
                    put("prioridad", rep.prioridad ?: "MEDIA")
                    put("colorHex", rep.semaforoColorHex ?: when (rep.prioridad) {
                        "CRITICA" -> "#F44336"
                        "ALTA" -> "#FF9800"
                        "MEDIA" -> "#FFEB3B"
                        else -> "#4CAF50"
                    })
                    put("cercanos", rep.reportesCercanos ?: 1)
                }
                jsonArray.put(obj)
            }
        }
        jsonArray.toString()
    }

    val userLat = userLocation?.first
    val userLng = userLocation?.second
    val pickLat = selectedLat ?: centerLat
    val pickLng = selectedLng ?: centerLng

    val htmlContent = remember(isPickerMode, reportesJson) {
        generateLeafletHtml(
            centerLat = centerLat,
            centerLng = centerLng,
            zoom = zoom,
            userLat = userLat,
            userLng = userLng,
            isPickerMode = isPickerMode,
            pickLat = pickLat,
            pickLng = pickLng,
            reportesJson = reportesJson
        )
    }

    // Efectos para comunicar cambios reactivos al mapa sin recargar la página entera
    LaunchedEffect(centerLat, centerLng, zoom, isMapLoaded) {
        if (isMapLoaded) {
            webViewRef?.evaluateJavascript(
                "if (window.centerMap) window.centerMap($centerLat, $centerLng, $zoom);",
                null
            )
        }
    }

    LaunchedEffect(userLat, userLng, isMapLoaded) {
        if (isMapLoaded && userLat != null && userLng != null) {
            webViewRef?.evaluateJavascript(
                "if (window.setUserLocation) window.setUserLocation($userLat, $userLng);",
                null
            )
        }
    }

    LaunchedEffect(pickLat, pickLng, isMapLoaded) {
        if (isMapLoaded && isPickerMode) {
            webViewRef?.evaluateJavascript(
                "if (window.setPickerLocation) window.setPickerLocation($pickLat, $pickLng);",
                null
            )
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewRef = this
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Habilitar aceleración por hardware para renderizado fluido de tiles
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    allowFileAccess = true
                    allowContentAccess = true
                    // User-Agent completo para evitar bloqueo 403 Forbidden de servidores de tiles
                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 CiudadActivaApp/1.0"
                }

                // Diagnóstico y logging de consola de JavaScript
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(TAG, "[Leaflet JS] ${consoleMessage?.message()} (línea ${consoleMessage?.lineNumber()})")
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isMapLoaded = true
                        Log.d(TAG, "Página Leaflet cargada con éxito. Forzando invalidateSize().")
                        // InvalidateSize tras cargar para garantizar que Leaflet detecte el tamaño real del viewport
                        view?.evaluateJavascript("if (window.map) { window.map.invalidateSize(); }", null)
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        Log.e(TAG, "Error cargando recurso: ${request?.url} -> ${error?.description}")
                    }
                }

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onReportSelected(idStr: String) {
                        val id = idStr.toLongOrNull() ?: return
                        Handler(Looper.getMainLooper()).post {
                            onReportClick?.invoke(id)
                        }
                    }

                    @JavascriptInterface
                    fun onLocationPicked(lat: Double, lng: Double) {
                        Handler(Looper.getMainLooper()).post {
                            onLocationSelected?.invoke(lat, lng)
                        }
                    }
                }, "AndroidBridge")

                loadDataWithBaseURL("https://cdnjs.cloudflare.com", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webViewRef = webView
        }
    )
}

private fun generateLeafletHtml(
    centerLat: Double,
    centerLng: Double,
    zoom: Int,
    userLat: Double?,
    userLng: Double?,
    isPickerMode: Boolean,
    pickLat: Double,
    pickLng: Double,
    reportesJson: String
): String {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <title>CiudadActiva Map</title>
            <!-- Leaflet CSS desde Cloudflare CDN -->
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.css" />
            <!-- Leaflet JS desde Cloudflare CDN -->
            <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.min.js"></script>
            <style>
                * { box-sizing: border-box; }
                html, body {
                    margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: #E6EAE8;
                }
                #map {
                    position: absolute; top: 0; bottom: 0; left: 0; right: 0;
                    width: 100vw; height: 100vh;
                    background-color: #E6EAE8;
                }
                .user-dot {
                    width: 20px; height: 20px; background: #007AFF; border: 3px solid #FFFFFF; border-radius: 50%;
                    box-shadow: 0 0 12px rgba(0, 122, 255, 0.75);
                    animation: pulse 2s infinite;
                }
                @keyframes pulse {
                    0% { box-shadow: 0 0 0 0 rgba(0, 122, 255, 0.7); }
                    70% { box-shadow: 0 0 0 14px rgba(0, 122, 255, 0); }
                    100% { box-shadow: 0 0 0 0 rgba(0, 122, 255, 0); }
                }
                .popup-card {
                    font-size: 13px; line-height: 1.4; padding: 4px; min-width: 190px;
                }
                .popup-title { font-weight: bold; font-size: 14px; color: #1E302F; margin-bottom: 4px; }
                .popup-cat { font-size: 11px; font-weight: 600; color: #00796B; background: #E0F2F1; padding: 3px 8px; border-radius: 8px; display: inline-block; margin-bottom: 6px; }
                .popup-btn {
                    display: block; width: 100%; margin-top: 8px; padding: 8px 0; background: #1E302F; color: white;
                    text-align: center; border-radius: 8px; text-decoration: none; font-size: 12px; font-weight: bold; border: none; cursor: pointer;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map;
                try {
                    map = L.map('map', {
                        zoomControl: false,
                        attributionControl: false
                    }).setView([$centerLat, $centerLng], $zoom);

                    L.control.zoom({ position: 'bottomright' }).addTo(map);

                    // OpenStreetMap estándar oficial: 100% libre, sin API key, sin marcas de agua
                    var osmLayer = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        maxZoom: 19,
                        attribution: '&copy; OpenStreetMap contributors'
                    }).addTo(map);

                    window.fallbackActive = false;
                    // Fallback automático de alta velocidad a ESRI World Street Map si falla alguna tesela
                    osmLayer.on('tileerror', function(error, tile) {
                        if (!window.fallbackActive) {
                            window.fallbackActive = true;
                            console.log('Activando capa de respaldo ESRI...');
                            L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
                                maxZoom: 19,
                                attribution: '&copy; Esri'
                            }).addTo(map);
                        }
                    });

                    window.map = map;
                    window.userMarker = null;
                    window.pickerMarker = null;

                    // Redimensionar automáticamente tras inicialización (Resuelve pantalla gris en WebViews móviles)
                    setTimeout(function() { if (map) map.invalidateSize(); }, 200);
                    setTimeout(function() { if (map) map.invalidateSize(); }, 600);
                    setTimeout(function() { if (map) map.invalidateSize(); }, 1500);

                    window.addEventListener('resize', function() {
                        if (map) map.invalidateSize();
                    });

                    window.centerMap = function(lat, lng, z) {
                        if (map) {
                            map.setView([lat, lng], z || 15);
                            map.invalidateSize();
                        }
                    };

                    window.setUserLocation = function(lat, lng) {
                        if (window.userMarker) {
                            window.userMarker.setLatLng([lat, lng]);
                        } else {
                            var userIcon = L.divIcon({
                                className: 'user-marker-container',
                                html: '<div class="user-dot"></div>',
                                iconSize: [22, 22],
                                iconAnchor: [11, 11]
                            });
                            window.userMarker = L.marker([lat, lng], { icon: userIcon }).addTo(map)
                                .bindPopup('<b>📍 Tu Ubicación Actual</b>');
                        }
                    };

                    window.setPickerLocation = function(lat, lng) {
                        if (window.pickerMarker) {
                            window.pickerMarker.setLatLng([lat, lng]);
                        }
                    };

                    ${if (userLat != null && userLng != null) """
                        window.setUserLocation($userLat, $userLng);
                    """ else ""}

                    ${if (isPickerMode) """
                        // Modo selector de ubicación para Nuevo Reporte
                        window.pickerMarker = L.marker([$pickLat, $pickLng], { draggable: true }).addTo(map);
                        window.pickerMarker.bindPopup('<b>📍 Ubicación Seleccionada</b><br>Arrastra o toca el mapa para fijar').openPopup();

                        window.pickerMarker.on('dragend', function(e) {
                            var pos = window.pickerMarker.getLatLng();
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onLocationPicked(pos.lat, pos.lng);
                            }
                        });

                        map.on('click', function(e) {
                            window.pickerMarker.setLatLng(e.latlng);
                            window.pickerMarker.openPopup();
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onLocationPicked(e.latlng.lat, e.latlng.lng);
                            }
                        });
                    """ else """
                        // Modo explorador: Graficar reportes comunitarios con color Semáforo
                        var reportesData = $reportesJson;
                        reportesData.forEach(function(rep) {
                            var circle = L.circleMarker([rep.latitud, rep.longitud], {
                                radius: 12,
                                fillColor: rep.colorHex,
                                color: '#FFFFFF',
                                weight: 2.5,
                                opacity: 1,
                                fillOpacity: 0.90
                            }).addTo(map);

                            var htmlPopup = '<div class="popup-card">' +
                                '<span class="popup-cat">' + rep.categoria + '</span>' +
                                '<div class="popup-title">' + rep.titulo + '</div>' +
                                '<div style="font-size: 11px; color: #555;">📍 ' + rep.direccion + '</div>' +
                                '<div style="font-size: 11px; font-weight: bold; margin-top: 5px; color: ' + rep.colorHex + ';">Urgencia: ' + rep.prioridad + ' (' + rep.cercanos + ' en sector)</div>' +
                                '<button class="popup-btn" onclick="window.AndroidBridge.onReportSelected(\'' + rep.id + '\')">Ver Seguimiento →</button>' +
                                '</div>';

                            circle.bindPopup(htmlPopup);
                        });
                    """}
                } catch (err) {
                    console.error('Error inicializando Leaflet: ' + err.message);
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
