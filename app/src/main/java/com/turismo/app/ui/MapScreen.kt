package com.turismo.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.config.Configuration
import com.turismo.app.data.Lugar

@Composable
fun MapScreen(
    viewModel: TurismoViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val estado by viewModel.ui.collectAsState()
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var permisosConcedidos by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val concedido = fineLocation || coarseLocation
        permisosConcedidos = concedido
        viewModel.permisoUbicacion(concedido)
        if (concedido) {
            myLocationOverlay?.enableMyLocation()
            myLocationOverlay?.enableFollowLocation()
            obtenerUbicacion(context, viewModel)
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        permisosConcedidos = fineGranted || coarseGranted
        viewModel.permisoUbicacion(permisosConcedidos)

        if (!permisosConcedidos) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        } else {
            obtenerUbicacion(context, viewModel)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().load(
                    ctx,
                    android.preference.PreferenceManager.getDefaultSharedPreferences(ctx),
                )
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)

                    myLocationOverlay = MyLocationNewOverlay(
                        FusedLocationProvider(context),
                        this,
                    ).apply {
                        enableMyLocation()
                    }
                    overlays.add(myLocationOverlay)

                    mapViewRef = this
                    cargando = false
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.overlays.removeAll { it is Marker }
                agregarLugaresAlMapa(mapView, estado.lugares)
            },
        )

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ComposeColor.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        FiltroCategoriasMapa(
            categorias = estado.categorias,
            onCategoriaClick = { categoria ->
                viewModel.filtrarPorCategoria(categoria)
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = {
                    val mv = mapViewRef
                    if (mv != null) {
                        myLocationOverlay?.myLocation?.let { location ->
                            centrarEnUbicacion(mv, location.latitude, location.longitude)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color.White,
                        shape = CircleShape,
                    )
                    .clip(CircleShape),
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Centrar en mi ubicacion",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private class FusedLocationProvider(private val context: Context) : IMyLocationProvider {
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var currentLocation: android.location.Location? = null

    @SuppressLint("MissingPermission")
    override fun startLocationProvider(consumer: IMyLocationConsumer): Boolean {
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                consumer.onLocationChanged(location, this)
            }
        }
        return true
    }

    override fun stopLocationProvider() {}

    override fun getLastKnownLocation(): android.location.Location? = currentLocation

    override fun destroy() {}
}

private fun obtenerUbicacion(context: Context, viewModel: TurismoViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    @SuppressLint("MissingPermission")
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                viewModel.actualizarUbicacion(location.latitude, location.longitude)
            }
        }
}

private fun centrarEnUbicacion(mapView: MapView, lat: Double, lng: Double) {
    val controller = mapView.controller
    controller.setZoom(15.0)
    controller.animateTo(GeoPoint(lat, lng))
}

private fun agregarLugaresAlMapa(mapView: MapView, lugares: List<Lugar>) {
    val lugaresConCoordenadas = lugares.filter { it.lat != null && it.lng != null }
    if (lugaresConCoordenadas.isEmpty()) return

    for (lugar in lugaresConCoordenadas) {
        val marker = Marker(mapView)
        marker.position = GeoPoint(lugar.lat!!, lugar.lng!!)
        marker.title = lugar.nombre
        marker.snippet = lugar.categoria
        marker.icon = createCategoryMarker(lugar.categoria)
        mapView.overlays.add(marker)
    }
}

private fun createCategoryMarker(categoria: String): ColorDrawable {
    val color = getCategoryColor(categoria)
    return ColorDrawable(color)
}

private fun getCategoryColor(categoria: String): Int {
    return when (categoria.lowercase()) {
        "restaurante", "restaurant" -> Color.parseColor("#FF6B6B")
        "hotel", "hospedaje" -> Color.parseColor("#4ECDC4")
        "museo" -> Color.parseColor("#45B7D1")
        "parque", "naturaleza" -> Color.parseColor("#96CEB4")
        "bar", "vida nocturna" -> Color.parseColor("#FECEA8")
        "tienda", "compras" -> Color.parseColor("#D4A5A5")
        "playa" -> Color.parseColor("#87CEEB")
        else -> Color.parseColor("#0D9488")
    }
}

@Composable
private fun FiltroCategoriasMapa(
    categorias: List<CategoriaItem>,
    onCategoriaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp,
    ) {
        LazyRow(
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(categorias) { cat ->
                FiltroChipMapa(
                    emoji = cat.icono,
                    nombre = cat.nombre,
                    onClick = { onCategoriaClick(cat.nombre) },
                )
            }
        }
    }
}

@Composable
private fun FiltroChipMapa(
    emoji: String,
    nombre: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = nombre,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
