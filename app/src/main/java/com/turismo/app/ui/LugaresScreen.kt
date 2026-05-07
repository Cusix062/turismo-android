package com.turismo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.turismo.app.data.Lugar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LugaresScreen(
    viewModel: TurismoViewModel,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.ui.collectAsState()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        val m = estado.mensaje
        if (m != null) {
            snack.showSnackbar(m)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Lugares") },
                actions = {
                    IconButton(onClick = { viewModel.refrescarLugares() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snack) },
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            if (estado.cargando && estado.lugares.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(estado.lugares, key = { it.id }) { lugar ->
                        TarjetaLugar(
                            lugar = lugar,
                            esFavorito = estado.favoritos.any { it.lugarId == lugar.id },
                            onFavorito = {
                                if (estado.favoritos.any { it.lugarId == lugar.id }) {
                                    viewModel.removerFavorito(lugar.id)
                                } else {
                                    viewModel.agregarFavorito(lugar.id)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaLugar(
    lugar: Lugar,
    esFavorito: Boolean,
    onFavorito: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (!lugar.imagen.isNullOrEmpty()) {
                AsyncImage(
                    model = lugar.imagen,
                    contentDescription = lugar.nombre,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.small),
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    lugar.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    lugar.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = lugar.categoria,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (lugar.popularidad != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\uD83D\uDD25 ${lugar.popularidad}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFF6B6B),
                        )
                    }
                }
            }
            IconButton(onClick = onFavorito) {
                Icon(
                    if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (esFavorito) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
