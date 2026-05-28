package com.turismo.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private enum class Pestana(
    val titulo: String,
    val icono: ImageVector,
) {
    Home("Inicio", Icons.Default.Explore),
    Lugares("Lugares", Icons.Default.Place),
    Mapa("Mapa", Icons.Default.LocationOn),
    Agregar("Agregar", Icons.Default.Add),
    Favoritos("Favoritos", Icons.Default.Favorite),
}

@Composable
fun TurismoRoot(viewModel: TurismoViewModel) {
    val ui by viewModel.ui.collectAsState()

    if (!ui.auth.logueado) {
        LoginScreen(
            onLogin = { email, password -> viewModel.login(email, password) },
            onRegister = { email, nombre, password -> viewModel.register(email, nombre, password) },
            mensaje = ui.auth.error,
            cargando = ui.auth.cargando,
        )
        return
    }

    // Cargar datos solo al iniciar sesion
    LaunchedEffect(ui.auth.logueado) {
        if (ui.auth.logueado) viewModel.refrescarTodo()
    }

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

            Pestana.Agregar -> AddPlaceScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(inner),
            )

            Pestana.Favoritos -> FavoritosScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(inner),
                onCerrarSesion = { viewModel.cerrarSesion() },
            )
        }
    }
}
