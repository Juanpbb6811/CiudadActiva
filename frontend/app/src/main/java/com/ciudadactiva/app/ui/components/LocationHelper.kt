package com.ciudadactiva.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun getCurrentLocation(
        context: Context,
        onLocationReceived: (Double, Double) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        if (!hasLocationPermission(context)) {
            onError?.invoke("Permisos de ubicación no otorgados")
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError?.invoke("Servicio de ubicación no disponible")
            return
        }

        try {
            val gpsLoc = try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
            val netLoc = try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }
            val passLoc = try { locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (_: Exception) { null }

            val bestLoc: Location? = listOfNotNull(gpsLoc, netLoc, passLoc).maxByOrNull { it.time }

            // Si hay una ubicación reciente (menos de 2 minutos)
            if (bestLoc != null && (System.currentTimeMillis() - bestLoc.time < 120_000)) {
                onLocationReceived(bestLoc.latitude, bestLoc.longitude)
                return
            }

            var callbackTriggered = false
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    if (!callbackTriggered) {
                        callbackTriggered = true
                        onLocationReceived(loc.latitude, loc.longitude)
                        try {
                            locationManager.removeUpdates(this)
                        } catch (_: Exception) {}
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val isNetworkEnabled = try { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { false }
            val isGpsEnabled = try { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }

            when {
                isNetworkEnabled -> {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000L,
                        2f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
                isGpsEnabled -> {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
                        2f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
                bestLoc != null -> {
                    onLocationReceived(bestLoc.latitude, bestLoc.longitude)
                }
                else -> {
                    onError?.invoke("El sensor GPS está inactivo")
                }
            }
        } catch (e: SecurityException) {
            onError?.invoke("Error de seguridad: ${e.localizedMessage}")
        } catch (e: Exception) {
            onError?.invoke("Error al obtener ubicación: ${e.localizedMessage}")
        }
    }
}
