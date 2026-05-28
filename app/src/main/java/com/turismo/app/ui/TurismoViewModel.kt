package com.turismo.app.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turismo.app.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class LocationState(
    val lat: Double? = null,
    val lng: Double? = null,
    val hasPermission: Boolean = false,
)

data class AuthState(
    val logueado: Boolean = false,
    val invitado: Boolean = false,
    val usuario: UsuarioToken? = null,
    val cargando: Boolean = false,
    val error: String? = null,
)

data class ComentariosState(
    val lista: List<Comentario> = emptyList(),
    val cargando: Boolean = false,
)

data class RutaState(
    val coordinates: List<Pair<Double, Double>> = emptyList(),
    val distance: Double = 0.0,
    val duration: Double = 0.0,
    val cargando: Boolean = false,
)

data class TurismoUiState(
    val lugares: List<Lugar> = emptyList(),
    val favoritos: List<FavoritoItem> = emptyList(),
    val populares: List<Lugar> = emptyList(),
    val nuevos: List<Lugar> = emptyList(),
    val suggestions: List<SearchSuggestion> = emptyList(),
    val categorias: List<CategoriaItem> = emptyList(),
    val location: LocationState = LocationState(),
    val cargando: Boolean = false,
    val cargandoHome: Boolean = false,
    val agregandoLugar: Boolean = false,
    val recomendados: List<Lugar> = emptyList(),
    val cercanos: List<Lugar> = emptyList(),
    val mensaje: String? = null,
    val auth: AuthState = AuthState(),
    val comentarios: ComentariosState = ComentariosState(),
    val ruta: RutaState = RutaState(),
    val lugarSeleccionado: Lugar? = null,
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
    }

    // ---- Auth ----

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(auth = _ui.value.auth.copy(cargando = true, error = null))
            runCatching {
                val res = api.login(AuthRequest(email, password))
                ApiClient.token = res.data.token
                _ui.value = _ui.value.copy(
                    auth = AuthState(logueado = true, usuario = res.data),
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    auth = _ui.value.auth.copy(cargando = false, error = "Email o contraseña incorrectos"),
                )
            }
        }
    }

    fun register(email: String, nombre: String, password: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(auth = _ui.value.auth.copy(cargando = true, error = null))
            runCatching {
                val res = api.register(AuthRequest(email, password, nombre))
                ApiClient.token = res.data.token
                _ui.value = _ui.value.copy(
                    auth = AuthState(logueado = true, usuario = res.data),
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    auth = _ui.value.auth.copy(cargando = false, error = "Error al registrarse. ¿El email ya existe?"),
                )
            }
        }
    }

    fun loguearComoInvitado() {
        ApiClient.token = null
        _ui.value = _ui.value.copy(
            auth = AuthState(logueado = true, invitado = true),
        )
    }

    fun cerrarSesion() {
        ApiClient.token = null
        _ui.value = TurismoUiState(
            categorias = _ui.value.categorias,
        )
    }

    // ---- Location ----

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

    // ---- Data loading ----

    fun refrescarTodo() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargando = true, mensaje = null)
            runCatching {
                val lugares = api.getLugares().data
                val favoritos = if (_ui.value.auth.logueado && !_ui.value.auth.invitado) {
                    api.getFavoritos().data
                } else emptyList()
                _ui.value = _ui.value.copy(
                    lugares = lugares, favoritos = favoritos, cargando = false,
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    cargando = false,
                    mensaje = e.message ?: "Error de red",
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
                    runCatching { api.getLugaresPopulares(lat = loc.lat, lng = loc.lng).data }
                        .getOrDefault(emptyList())
                }
                val nuevosDeferred = async {
                    runCatching { api.getLugaresNuevos().data }.getOrDefault(emptyList())
                }
                val favoritosDeferred = async {
                    if (_ui.value.auth.logueado && !_ui.value.auth.invitado) {
                        runCatching { api.getFavoritos().data }.getOrDefault(emptyList())
                    } else emptyList()
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
                    mensaje = e.message ?: "Error al filtrar",
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
                val favoritos = api.getFavoritos().data
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
                api.addFavorito(AddFavoritoBody(lugarId))
                val favoritos = api.getFavoritos().data
                _ui.value = _ui.value.copy(favoritos = favoritos)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    mensaje = e.message ?: "No se pudo añadir favorito",
                )
            }
        }
    }

    fun removerFavorito(lugarId: Int) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(mensaje = null)
            runCatching {
                api.removeFavorito(lugarId)
                val favoritos = api.getFavoritos().data
                _ui.value = _ui.value.copy(favoritos = favoritos)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    mensaje = e.message ?: "No se pudo eliminar favorito",
                )
            }
        }
    }

    // ---- Recomendados ----

    fun cargarRecomendados() {
        viewModelScope.launch {
            val loc = _ui.value.location
            runCatching {
                val res = api.getLugaresRecomendados(lat = loc.lat, lng = loc.lng, limite = 10)
                _ui.value = _ui.value.copy(recomendados = res.data)
            }
        }
    }

    // ---- Cercanos ----

    fun cargarCercanos(radio: Double = 10.0) {
        viewModelScope.launch {
            val loc = _ui.value.location
            if (loc.lat == null || loc.lng == null) return@launch
            runCatching {
                val res = api.getLugaresCercanos(loc.lat, loc.lng, radio)
                _ui.value = _ui.value.copy(cercanos = res.data)
            }
        }
    }

    // ---- Add Place ----

    fun agregarLugar(nombre: String, descripcion: String, categoria: String, lat: Double, lng: Double, direccion: String?, horario: String?, imageUri: Uri?, context: Context) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(agregandoLugar = true, mensaje = null)
            runCatching {
                val nombrePart = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                val descripcionPart = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoriaPart = categoria.toRequestBody("text/plain".toMediaTypeOrNull())
                val latPart = lat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val lngPart = lng.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val direccionPart = direccion?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val horarioPart = horario?.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = imageUri?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("imagen", "photo.jpg", requestBody)
                    } else null
                }

                api.createLugar(nombrePart, descripcionPart, categoriaPart, latPart, lngPart, direccionPart, horarioPart, imagePart)
                _ui.value = _ui.value.copy(agregandoLugar = false, mensaje = "Lugar creado exitosamente!")
                refrescarLugares()
            }.onFailure { e ->
                _ui.value = _ui.value.copy(agregandoLugar = false, mensaje = e.message ?: "Error al crear lugar")
            }
        }
    }

    fun limpiarAgregarLugar() {
        _ui.value = _ui.value.copy(mensaje = null, agregandoLugar = false)
    }

    // ---- Visitas ----

    fun visitarLugar(lugarId: Int) {
        viewModelScope.launch {
            runCatching { api.visitarLugar(lugarId) }
        }
    }

    // ---- Comments ----

    fun cargarComentarios(lugarId: Int) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                comentarios = ComentariosState(cargando = true),
            )
            runCatching {
                val lista = api.getComentarios(lugarId).data
                _ui.value = _ui.value.copy(
                    comentarios = ComentariosState(lista = lista),
                )
            }.onFailure {
                _ui.value = _ui.value.copy(
                    comentarios = ComentariosState(cargando = false),
                )
            }
        }
    }

    fun agregarComentario(lugarId: Int, texto: String, calificacion: Int = 5) {
        viewModelScope.launch {
            runCatching {
                api.addComentario(lugarId, AddComentarioBody(texto, calificacion))
                cargarComentarios(lugarId)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    mensaje = e.message ?: "Error al enviar comentario",
                )
            }
        }
    }

    // ---- Routes ----

    fun calcularRuta(origenLat: Double, origenLng: Double, destinoLat: Double, destinoLng: Double) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(ruta = RutaState(cargando = true))
            runCatching {
                val res = api.getRuta("$origenLng,$origenLat", "$destinoLng,$destinoLat")
                val ruta = res.routes?.firstOrNull()
                if (ruta != null) {
                    val coords = ruta.geometry.coordinates.map { (lng, lat) ->
                        Pair(lat, lng)
                    }
                    _ui.value = _ui.value.copy(
                        ruta = RutaState(
                            coordinates = coords,
                            distance = ruta.distance,
                            duration = ruta.duration,
                        ),
                    )
                } else {
                    _ui.value = _ui.value.copy(
                        ruta = RutaState(),
                        mensaje = "No se encontró ruta",
                    )
                }
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    ruta = RutaState(),
                    mensaje = e.message ?: "Error al calcular ruta",
                )
            }
        }
    }

    fun limpiarRuta() {
        _ui.value = _ui.value.copy(ruta = RutaState())
    }

    fun seleccionarLugar(lugar: Lugar?) {
        _ui.value = _ui.value.copy(lugarSeleccionado = lugar)
    }

    fun limpiarMensaje() {
        _ui.value = _ui.value.copy(mensaje = null)
    }
}
