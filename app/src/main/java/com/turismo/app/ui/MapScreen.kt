package com.turismo.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.*
import com.turismo.app.data.Lugar

@Composable
fun MapScreen(
    viewModel: TurismoViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val estado by viewModel.ui.collectAsState()
    var permisosConcedidos by remember { mutableStateOf(false) }
    var uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val concedido = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)
        permisosConcedidos = concedido
        viewModel.permisoUbicacion(concedido)
        if (concedido) {
            obtenerUbicacionMapa(context, viewModel)
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        permisosConcedidos = fineGranted || coarseGranted
        viewModel.permisoUbicacion(permisosConcedidos)
        if (!permisosConcedidos) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            obtenerUbicacionMapa(context, viewModel)
        }
    }

    val userLocation = estado.location.let { loc ->
        if (loc.lat != null && loc.lng != null) LatLng(loc.lat, loc.lng) else null
    }

    val defaultPosition = userLocation ?: LatLng(19.4326, -99.1332)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 10f)
    }

    var selectedPlace by remember { mutableStateOf<Lugar?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            onMapClick = { selectedPlace = null; viewModel.limpiarRuta() },
        ) {
            if (userLocation != null) {
                Marker(
                    state = rememberMarkerState(position = userLocation),
                    title = "Tu ubicacion",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                )
            }

            for (lugar in estado.lugares.filter { it.lat != null && it.lng != null }) {
                val pos = LatLng(lugar.lat!!, lugar.lng!!)
                val markerState = rememberMarkerState(position = pos)
                Marker(
                    state = markerState,
                    title = lugar.nombre,
                    snippet = lugar.categoria,
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue(lugar.categoria)),
                    onClick = {
                        selectedPlace = lugar
                        viewModel.cargarComentarios(lugar.id)
                        false
                    },
                )
            }

            if (estado.ruta.coordinates.isNotEmpty()) {
                val coords = estado.ruta.coordinates.map { LatLng(it.first, it.second) }
                Polyline(
                    points = coords,
                    color = ComposeColor(0xFF0D9488),
                    width = 6f,
                )
            }
        }

        // Filtro categorias
        FiltroCategoriasMapa(
            categorias = estado.categorias,
            onCategoriaClick = { viewModel.filtrarPorCategoria(it) },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )

        // Boton centrar ubicacion
        Column(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = (if (selectedPlace != null) 220 else 16).dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FloatingActionButton(
                onClick = {
                    userLocation?.let { loc ->
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 15f)
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
                                    // Animate camera to show route
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                        LatLng((loc.lat + lugar.lat) / 2, (loc.lng + lugar.lng) / 2), 12f
                                    )
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

private fun obtenerUbicacionMapa(context: android.content.Context, viewModel: TurismoViewModel) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                viewModel.actualizarUbicacion(location.latitude, location.longitude)
            }
        }
}