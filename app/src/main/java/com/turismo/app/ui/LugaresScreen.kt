package com.turismo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.turismo.app.BuildConfig
import com.turismo.app.data.Comentario
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

    var lugarComentarios by remember { mutableStateOf<Lugar?>(null) }

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
                            onClick = {
                                lugarComentarios = lugar
                                viewModel.cargarComentarios(lugar.id)
                            },
                        )
                    }
                }
            }
        }
    }

    // Dialog de comentarios
    lugarComentarios?.let { lugar ->
        DialogComentarios(
            lugar = lugar,
            comentarios = estado.comentarios,
            onEnviarComentario = { texto, calificacion ->
                viewModel.agregarComentario(lugar.id, texto, calificacion)
            },
            onDismiss = {
                lugarComentarios = null
            },
        )
    }
}

@Composable
private fun TarjetaLugar(
    lugar: Lugar,
    esFavorito: Boolean,
    onFavorito: () -> Unit,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (!lugar.imagen.isNullOrEmpty()) {
                val imageUrl = if (lugar.imagen.startsWith("/")) {
                    BuildConfig.API_BASE_URL.trimEnd('/') + lugar.imagen
                } else lugar.imagen
                AsyncImage(
                    model = imageUrl,
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
                    if (lugar.visitas != null && lugar.visitas > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\uD83D\uDC40 ${lugar.visitas}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6B7280),
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

@Composable
private fun DialogComentarios(
    lugar: Lugar,
    comentarios: ComentariosState,
    onEnviarComentario: (texto: String, calificacion: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var textoComentario by remember { mutableStateOf("") }
    var calificacion by remember { mutableIntStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(lugar.nombre, style = MaterialTheme.typography.titleLarge)
                if (lugar.direccion != null) {
                    Text(lugar.direccion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                if (lugar.horario != null) {
                    Text("Horario: ${lugar.horario}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text("Comentarios", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (comentarios.cargando) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (comentarios.lista.isEmpty()) {
                    Text("No hay comentarios aun. Se el primero en opinar!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(comentarios.lista, key = { it.id }) { c ->
                            ComentarioItem(c)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Calificacion:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    for (i in 1..5) {
                        Text(
                            if (i <= calificacion) "\u2B50" else "\u2606",
                            modifier = Modifier.clickable { calificacion = i },
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = textoComentario,
                    onValueChange = { textoComentario = it },
                    label = { Text("Escribe un comentario...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 4,
                )

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (textoComentario.isNotBlank()) {
                                onEnviarComentario(textoComentario, calificacion)
                                textoComentario = ""
                            }
                        },
                        enabled = textoComentario.isNotBlank(),
                    ) { Text("Enviar") }
                }
            }
        }
    }
}

@Composable
private fun ComentarioItem(comentario: Comentario) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comentario.usuarioNombre, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    (1..comentario.calificacion).joinToString("") { "\u2B50" },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(comentario.texto, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
