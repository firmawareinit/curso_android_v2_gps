package com.curso.android.ubicaciongps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.curso.android.ubicaciongps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Definimos constantes
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val LOCATION_UPDATE_INTERVAL = 5000L // 10 segundos
    }

    // Declaramos las variables necesarias
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Inicializamos el view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializamos el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verificamos los permisos al iniciar la actividad
        checkLocationPermission()
    }

    // Función para verificar el permiso de ubicación
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no tenemos el permiso, lo solicitamos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Si ya tenemos el permiso, iniciamos la obtención de ubicación
            startLocationUpdates()
        }
    }

    // Función para iniciar las actualizaciones de ubicación
    private fun startLocationUpdates() {
        // Configuramos la solicitud de ubicación
        val locationRequest = LocationRequest.Builder(LOCATION_UPDATE_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        // Definimos el callback para recibir actualizaciones de ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Actualizamos la UI con la nueva ubicación
                    updateLocationUI(location.latitude, location.longitude)
                }
            }
        }

        // Verificamos el permiso nuevamente (requerido por Android)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitamos actualizaciones de ubicación
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    // Función para actualizar la UI con la nueva ubicación
    private fun updateLocationUI(latitude: Double, longitude: Double) {
        binding.tvLatitud.text = "Latitud: $latitude"
        binding.tvLongitud.text = "Longitud: $longitude"
    }

    // Manejamos el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, iniciamos la obtención de ubicación
                startLocationUpdates()
            } else {
                // Permiso denegado, informamos al usuario
                Toast.makeText(
                    this,
                    "Se requiere permiso de ubicación para funcionar",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Detenemos las actualizaciones de ubicación cuando la actividad se detiene
    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}