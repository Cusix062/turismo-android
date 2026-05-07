package com.turismo.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private enum class Pestana(
    val titulo: String,
    val icono: ImageVector,
) {
    Home("Inicio", Icons.Default.Explore),
    Lugares("Lugares", Icons.Default.Place),
    Mapa("Mapa", Icons.Default.LocationOn),
    Favoritos("Favoritos", Icons.Default.Favorite),
}

@Composable
fun TurismoRoot(viewModel: TurismoViewModel) {
    var indice by rememberSaveable { mutableIntStateOf(0) }
    val pestana = Pestana.entries[indice]

    Scaffold(
        bottomBar = {
            NavigationBar {
                Pestana.entries.forEachIndexed { i, p ->
                    NavigationBarItem(
                        selected = indice == i,
                        onClick = { indice = i },
                        icon = { Icon(p.icono, contentDescription = p.titulo) },
                        label = { Text(p.titulo) },
                    )
                }
            }
        },
    ) { inner ->
        when (pestana) {
            Pestana.Home -> HomeScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(inner),
                onIrMapa = { indice = Pestana.Mapa.ordinal },
                onIrLugares = { indice = Pestana.Lugares.ordinal },
            )

            Pestana.Lugares -> LugaresScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(inner),
            )

            Pestana.Mapa -> MapScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(inner),
            )

            Pestana.Favoritos -> FavoritosScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(inner),
            )
        }
    }
}
