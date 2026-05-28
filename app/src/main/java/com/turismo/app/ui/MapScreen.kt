package com.turismo.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.turismo.app.data.Lugar

@Composable
fun MapScreen(
    viewModel: TurismoViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val estado by viewModel.ui.collectAsState()
    var permisosConcedidos by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<Lugar?>(null) }
    var mapReady by remember { mutableStateOf(false) }
    var playServicesOk by remember { mutableStateOf(true) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    // Check Play Services
    LaunchedEffect(Unit) {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (result != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            playServicesOk = false
        }
    }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val concedido = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)
        permisosConcedidos = concedido
        viewModel.permisoUbicacion(concedido)
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        permisosConcedidos = fineGranted || coarseGranted
        viewModel.permisoUbicacion(permisosConcedidos)
        if (!permisosConcedidos) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    if (!playServicesOk) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Google Play Services no está disponible en este dispositivo", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null)
                    onResume()
                    getMapAsync { map ->
                        googleMap = map
                        map.uiSettings.isZoomControlsEnabled = false
                        map.uiSettings.isMyLocationButtonEnabled = false
                        map.uiSettings.isMapToolbarEnabled = false
                        map.setMinZoomPreference(5f)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(19.4326, -99.1332), 10f))
                        map.setOnMarkerClickListener { marker ->
                            val lugar = marker.tag as? Lugar
                            if (lugar != null) {
                                selectedPlace = lugar
                                viewModel.cargarComentarios(lugar.id)
                            }
                            true
                        }
                        map.setOnMapClickListener {
                            selectedPlace = null
                            viewModel.limpiarRuta()
                        }
                        mapReady = true
                    }
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                val map = googleMap ?: return@AndroidView
                map.clear()

                // User location marker
                val loc = estado.location
                if (loc.lat != null && loc.lng != null) {
                    map.addMarker(MarkerOptions()
                        .position(LatLng(loc.lat, loc.lng))
                        .title("Tu ubicacion")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                }

                // Place markers
                for (lugar in estado.lugares) {
                    val lat = lugar.lat ?: continue
                    val lng = lugar.lng ?: continue
                    val marker = map.addMarker(MarkerOptions()
                        .position(LatLng(lat, lng))
                        .title(lugar.nombre)
                        .snippet(lugar.categoria)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerHue(lugar.categoria))))
                    marker?.tag = lugar
                }

                // Route polyline
                if (estado.ruta.coordinates.isNotEmpty()) {
                    val coords = estado.ruta.coordinates.map { LatLng(it.first, it.second) }
                    map.addPolyline(PolylineOptions()
                        .addAll(coords)
                        .color(0xFF0D9488.toInt())
                        .width(6f))
                }
            },
        )

        if (!mapReady) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // Filtro categorias
        FiltroCategoriasMapa(
            categorias = estado.categorias,
            onCategoriaClick = { viewModel.filtrarPorCategoria(it) },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )

        // Boton centrar ubicacion
        Column(
            Modifier.align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedPlace != null) 220.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FloatingActionButton(
                onClick = {
                    val loc = estado.location
                    if (loc.lat != null && loc.lng != null) {
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.lat, loc.lng), 15f))
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Icon(Icons.Default.MyLocation, "Centrar", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Info card for selected marker
        selectedPlace?.let { lugar ->
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(lugar.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(lugar.categoria, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    if (lugar.direccion != null) {
                        Text(lugar.direccion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val loc = estado.location
                                if (loc.lat != null && loc.lng != null && lugar.lat != null && lugar.lng != null) {
                                    viewModel.calcularRuta(loc.lat, loc.lng, lugar.lat, lugar.lng)
                                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        LatLng((loc.lat + lugar.lat) / 2, (loc.lng + lugar.lng) / 2), 12f
                                    ))
                                }
                            },
                            enabled = estado.location.lat != null && lugar.lat != null,
                        ) {
                            Icon(Icons.Default.Directions, "Ruta", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Como llegar")
                        }
                        OutlinedButton(onClick = { selectedPlace = null; viewModel.limpiarRuta() }) {
                            Text("Cerrar")
                        }
                    }
                    if (estado.ruta.cargando) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    if (estado.ruta.distance > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Distancia: ${"%.1f".format(estado.ruta.distance / 1000)} km | Tiempo: ${"%.0f".format(estado.ruta.duration / 60)} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

private fun markerHue(categoria: String): Float {
    return when (categoria.lowercase()) {
        "restaurante" -> BitmapDescriptorFactory.HUE_RED
        "playa" -> BitmapDescriptorFactory.HUE_CYAN
        "museo" -> BitmapDescriptorFactory.HUE_BLUE
        "parque" -> BitmapDescriptorFactory.HUE_GREEN
        "monumento" -> BitmapDescriptorFactory.HUE_ORANGE
        "mirador" -> BitmapDescriptorFactory.HUE_ROSE
        "centro historico" -> BitmapDescriptorFactory.HUE_VIOLET
        else -> BitmapDescriptorFactory.HUE_GREEN
    }
}

@Composable
private fun FiltroCategoriasMapa(
    categorias: List<CategoriaItem>,
    onCategoriaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, shadowElevation = 4.dp) {
        LazyRow(contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categorias) { cat ->
                Card(onClick = { onCategoriaClick(cat.nombre) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.icono, style = MaterialTheme.typography.bodyMedium)
                        Text(cat.nombre, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}