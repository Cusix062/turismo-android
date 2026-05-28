package com.turismo.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    viewModel: TurismoViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val estado by viewModel.ui.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var selectedLat by remember { mutableStateOf(19.4326) }
    var selectedLng by remember { mutableStateOf(-99.1332) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(selectedLat, selectedLng), 12f)
    }

    // Photo picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri
    }

    val categorias = listOf("Restaurante", "Playa", "Museo", "Parque", "Monumento", "Mirador", "Centro Historico")

    LaunchedEffect(estado.mensaje) {
        val m = estado.mensaje
        if (m != null) {
            snack.showSnackbar(m)
            viewModel.limpiarMensaje()
        }
    }

    // Get user location for default lat/lng
    LaunchedEffect(estado.location.lat, estado.location.lng) {
        val loc = estado.location
        if (loc.lat != null && loc.lng != null) {
            selectedLat = loc.lat
            selectedLng = loc.lng
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(loc.lat, loc.lng), 14f)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Agregar Lugar") },
                actions = {
                    IconButton(onClick = { viewModel.limpiarAgregarLugar() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snack) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Foto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Spacer(Modifier.height(8.dp))
                                Text("Toca para agregar foto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier.matchParentSize().clickable { photoPicker.launch("image/*") },
                ) {}
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del lugar *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripcion *") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                maxLines = 5,
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoriaExpanded,
                onExpandedChange = { categoriaExpanded = it },
            ) {
                OutlinedTextField(
                    value = categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = categoriaExpanded,
                    onDismissRequest = { categoriaExpanded = false },
                ) {
                    categorias.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { categoria = cat; categoriaExpanded = false },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Direccion") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = horario,
                onValueChange = { horario = it },
                label = { Text("Horario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Mini map for location
            Text("Selecciona ubicacion en el mapa:", style = MaterialTheme.typography.labelMedium)
            Surface(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = true, zoomGesturesEnabled = true),
                    onMapClick = { latLng ->
                        selectedLat = latLng.latitude
                        selectedLng = latLng.longitude
                    },
                ) {
                    val markerPos = remember(selectedLat, selectedLng) { LatLng(selectedLat, selectedLng) }
                    Marker(
                        state = rememberMarkerState(key = "picker", position = markerPos),
                        title = "Ubicacion seleccionada",
                        draggable = true,
                    )
                }
            }
            Text("Lat: ${"%.4f".format(selectedLat)}, Lng: ${"%.4f".format(selectedLng)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(8.dp))

            // Save
            Button(
                onClick = {
                    viewModel.agregarLugar(nombre, descripcion, categoria, selectedLat, selectedLng, direccion, horario, imageUri, context)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank() && descripcion.isNotBlank() && categoria.isNotBlank(),
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar Lugar")
            }
        }
    }
}