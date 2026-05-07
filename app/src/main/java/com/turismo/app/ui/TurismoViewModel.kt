package com.turismo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turismo.app.data.ApiClient
import com.turismo.app.data.AddFavoritoBody
import com.turismo.app.data.FavoritoItem
import com.turismo.app.data.Lugar
import com.turismo.app.data.SearchSuggestion
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationState(
    val lat: Double? = null,
    val lng: Double? = null,
    val hasPermission: Boolean = false,
)

data class TurismoUiState(
    val lugares: List<Lugar> = emptyList(),
    val favoritos: List<FavoritoItem> = emptyList(),
    val populares: List<Lugar> = emptyList(),
    val nuevos: List<Lugar> = emptyList(),
    val suggestions: List<SearchSuggestion> = emptyList(),
    val categorias: List<CategoriaItem> = emptyList(),
    val location: LocationState = LocationState(),
    val usuarioDemoId: Int = 1,
    val cargando: Boolean = false,
    val cargandoHome: Boolean = false,
    val mensaje: String? = null,
)

data class CategoriaItem(
    val nombre: String,
    val icono: String,
)

class TurismoViewModel : ViewModel() {

    private val api = ApiClient.api

    private val _ui = MutableStateFlow(TurismoUiState())
    val ui: StateFlow<TurismoUiState> = _ui.asStateFlow()

    private var searchJob: Job? = null

    init {
        _ui.value = _ui.value.copy(
            categorias = listOf(
                CategoriaItem("Restaurante", "\uD83C\uDF7D\uFE0F"),
                CategoriaItem("Playa", "\uD83C\uDFD6\uFE0F"),
                CategoriaItem("Museo", "\uD83C\uDFDB\uFE0F"),
                CategoriaItem("Parque", "\uD83C\uDF33"),
                CategoriaItem("Monumento", "\uD83C\uDFDB\uFE0F"),
                CategoriaItem("Mirador", "\uD83D\uDD2D"),
                CategoriaItem("Centro Historico", "\uD83C\uDFE2"),
            ),
        )
        refrescarTodo()
    }

    fun actualizarUbicacion(lat: Double, lng: Double) {
        _ui.value = _ui.value.copy(
            location = _ui.value.location.copy(lat = lat, lng = lng),
        )
    }

    fun permisoUbicacion(concedido: Boolean) {
        _ui.value = _ui.value.copy(
            location = _ui.value.location.copy(hasPermission = concedido),
        )
    }

    fun refrescarTodo() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargando = true, mensaje = null)
            runCatching {
                val lugares = api.getLugares().data
                val favoritos = api.getFavoritos(_ui.value.usuarioDemoId).data
                _ui.value = _ui.value.copy(
                    lugares = lugares,
                    favoritos = favoritos,
                    cargando = false,
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    cargando = false,
                    mensaje = e.message ?: "Error de red. ¿Está el backend en marcha?",
                )
            }
        }
    }

    fun cargarHome() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargandoHome = true, mensaje = null)
            runCatching {
                val loc = _ui.value.location
                val lugaresDeferred = async { api.getLugares().data }
                val popularesDeferred = async {
                    runCatching {
                        api.getLugaresPopulares(lat = loc.lat, lng = loc.lng).data
                    }.getOrDefault(emptyList())
                }
                val nuevosDeferred = async {
                    runCatching {
                        api.getLugaresNuevos().data
                    }.getOrDefault(emptyList())
                }
                val favoritosDeferred = async {
                    api.getFavoritos(_ui.value.usuarioDemoId).data
                }

                _ui.value = _ui.value.copy(
                    lugares = lugaresDeferred.await(),
                    populares = popularesDeferred.await(),
                    nuevos = nuevosDeferred.await(),
                    favoritos = favoritosDeferred.await(),
                    cargandoHome = false,
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    cargandoHome = false,
                    mensaje = e.message ?: "Error al cargar datos",
                )
            }
        }
    }

    fun buscarLugares(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _ui.value = _ui.value.copy(suggestions = emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            runCatching {
                val resultado = api.searchLugares(query).data
                _ui.value = _ui.value.copy(suggestions = resultado)
            }.onFailure {
                _ui.value = _ui.value.copy(suggestions = emptyList())
            }
        }
    }

    fun filtrarPorCategoria(categoria: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargando = true, mensaje = null)
            runCatching {
                val lugares = api.getLugaresPorCategoria(categoria).data
                _ui.value = _ui.value.copy(lugares = lugares, cargando = false)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    cargando = false,
                    mensaje = e.message ?: "Error al filtrar por categoría",
                )
            }
        }
    }

    fun refrescarLugares() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargando = true, mensaje = null)
            runCatching {
                val lugares = api.getLugares().data
                _ui.value = _ui.value.copy(lugares = lugares, cargando = false)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    cargando = false,
                    mensaje = e.message ?: "Error al cargar lugares",
                )
            }
        }
    }

    fun refrescarFavoritos() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargando = true, mensaje = null)
            runCatching {
                val favoritos = api.getFavoritos(_ui.value.usuarioDemoId).data
                _ui.value = _ui.value.copy(favoritos = favoritos, cargando = false)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    cargando = false,
                    mensaje = e.message ?: "Error al cargar favoritos",
                )
            }
        }
    }

    fun agregarFavorito(lugarId: Int) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(mensaje = null)
            runCatching {
                api.addFavorito(_ui.value.usuarioDemoId, AddFavoritoBody(lugarId))
                val favoritos = api.getFavoritos(_ui.value.usuarioDemoId).data
                _ui.value = _ui.value.copy(favoritos = favoritos)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    mensaje = e.message ?: "No se pudo añadir favorito (¿ya existe?)",
                )
            }
        }
    }

    fun removerFavorito(lugarId: Int) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(mensaje = null)
            runCatching {
                api.removeFavorito(_ui.value.usuarioDemoId, lugarId)
                val favoritos = api.getFavoritos(_ui.value.usuarioDemoId).data
                _ui.value = _ui.value.copy(favoritos = favoritos)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    mensaje = e.message ?: "No se pudo eliminar favorito",
                )
            }
        }
    }

    fun limpiarMensaje() {
        _ui.value = _ui.value.copy(mensaje = null)
    }
}
