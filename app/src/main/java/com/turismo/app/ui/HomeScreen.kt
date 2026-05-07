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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.turismo.app.data.Lugar
import com.turismo.app.data.SearchSuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TurismoViewModel,
    modifier: Modifier = Modifier,
    onIrMapa: () -> Unit = {},
    onIrLugares: () -> Unit = {},
) {
    val estado by viewModel.ui.collectAsState()
    val snack = remember { SnackbarHostState() }
    var busqueda by remember { mutableStateOf("") }
    var mostrarSugerencias by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarHome()
    }

    LaunchedEffect(estado.mensaje) {
        val m = estado.mensaje
        if (m != null) {
            snack.showSnackbar(m)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snack) },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                CabeceraHome(
                    onIrMapa = onIrMapa,
                    onRefrescar = { viewModel.cargarHome() },
                )
            }

            item {
                BarraBusqueda(
                    query = busqueda,
                    onQueryChange = { query ->
                        busqueda = query
                        mostrarSugerencias = query.length >= 2
                        viewModel.buscarLugares(query)
                    },
                    onSuggestionClick = { suggestion ->
                        busqueda = suggestion.nombre
                        mostrarSugerencias = false
                    },
                    suggestions = estado.suggestions,
                    mostrarSugerencias = mostrarSugerencias && busqueda.length >= 2,
                    onSearch = {
                        mostrarSugerencias = false
                        onIrLugares()
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (estado.location.hasPermission && estado.location.lat != null) {
                item {
                    SeccionCercanos(
                        onVerTodos = onIrLugares,
                    )
                }
            }

            item {
                Categorias(
                    categorias = estado.categorias,
                    onCategoriaClick = { categoria ->
                        viewModel.filtrarPorCategoria(categoria)
                        onIrLugares()
                    },
                )
            }

            if (estado.populares.isNotEmpty()) {
                item {
                    SeccionHorizontal(
                        titulo = "Lugares populares",
                        emoji = "\uD83D\uDD25",
                        lugares = estado.populares,
                        favoritosIds = estado.favoritos.map { it.lugarId },
                        onVerTodos = onIrLugares,
                        onFavorito = { viewModel.agregarFavorito(it) },
                        onRemoverFavorito = { viewModel.removerFavorito(it) },
                    )
                }
            }

            if (estado.nuevos.isNotEmpty()) {
                item {
                    SeccionHorizontal(
                        titulo = "Nuevos lugares",
                        emoji = "\u2728",
                        lugares = estado.nuevos,
                        favoritosIds = estado.favoritos.map { it.lugarId },
                        onVerTodos = onIrLugares,
                        onFavorito = { viewModel.agregarFavorito(it) },
                        onRemoverFavorito = { viewModel.removerFavorito(it) },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "Todos los lugares",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (estado.cargandoHome && estado.lugares.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(estado.lugares, key = { it.id }) { lugar ->
                    TarjetaLugarHome(
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

@Composable
private fun CabeceraHome(
    onIrMapa: () -> Unit,
    onRefrescar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(20.dp)
            .padding(top = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Turismo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
                Text(
                    text = "Descubre lugares increíbles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row {
                IconButton(onClick = onIrMapa) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Ver mapa",
                        tint = Color.White,
                    )
                }
                IconButton(onClick = onRefrescar) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraBusqueda(
    query: String,
    onQueryChange: (String) -> Unit,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    suggestions: List<SearchSuggestion>,
    mostrarSugerencias: Boolean,
    onSearch: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar lugares, categorías...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = MaterialTheme.shapes.medium,
        )

        if (mostrarSugerencias && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(8.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    suggestions.take(5).forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = suggestion.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = suggestion.categoria,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionCercanos(
    onVerTodos: () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "\uD83D\uDCCDCerca de ti",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Ver todo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onVerTodos() },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun Categorias(
    categorias: List<CategoriaItem>,
    onCategoriaClick: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(categorias) { cat ->
                ChipCategoria(
                    emoji = cat.icono,
                    nombre = cat.nombre,
                    onClick = { onCategoriaClick(cat.nombre) },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ChipCategoria(
    emoji: String,
    nombre: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun SeccionHorizontal(
    titulo: String,
    emoji: String,
    lugares: List<Lugar>,
    favoritosIds: List<Int>,
    onVerTodos: () -> Unit,
    onFavorito: (Int) -> Unit,
    onRemoverFavorito: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$emoji $titulo",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Ver todo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onVerTodos() },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(lugares, key = { it.id }) { lugar ->
                TarjetaHorizontalLugar(
                    lugar = lugar,
                    esFavorito = favoritosIds.contains(lugar.id),
                    onFavorito = {
                        if (favoritosIds.contains(lugar.id)) {
                            onRemoverFavorito(lugar.id)
                        } else {
                            onFavorito(lugar.id)
                        }
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TarjetaHorizontalLugar(
    lugar: Lugar,
    esFavorito: Boolean,
    onFavorito: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(160.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!lugar.imagen.isNullOrEmpty()) {
                AsyncImage(
                    model = lugar.imagen,
                    contentDescription = lugar.nombre,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "\uD83D\uDCCD",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = CircleShape,
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onFavorito),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (esFavorito) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                    )
                    .padding(10.dp),
            ) {
                Text(
                    text = lugar.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TarjetaLugarHome(
    lugar: Lugar,
    esFavorito: Boolean,
    onFavorito: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!lugar.imagen.isNullOrEmpty()) {
                AsyncImage(
                    model = lugar.imagen,
                    contentDescription = lugar.nombre,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.small),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.shapes.small,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "\uD83D\uDCCD", style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lugar.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    lugar.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    lugar.categoria,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
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
