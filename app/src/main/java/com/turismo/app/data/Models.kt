package com.turismo.app.data

import com.google.gson.annotations.SerializedName

data class ListaLugaresResponse(val data: List<Lugar>)
data class PlaceDetailResponse(val data: Lugar?)
data class SearchResponse(val data: List<SearchSuggestion>)

data class Lugar(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val lat: Double?,
    val lng: Double?,
    val imagen: String? = null,
    val popularidad: Int? = null,
    val visitas: Int? = null,
    val distanciaKm: Double? = null,
    val score: Double? = null,
    val fechaCreacion: String? = null,
    val direccion: String? = null,
    val horario: String? = null,
    val creadoPor: Int? = null,
)

data class SearchSuggestion(
    val id: Int,
    val nombre: String,
    val categoria: String,
)

// Auth
data class AuthRequest(val email: String, val password: String, val nombre: String? = null)

data class AuthResponse(val data: UsuarioToken)

data class UsuarioToken(
    val id: Int,
    val email: String,
    val nombre: String,
    val token: String,
)

data class Usuario(
    val id: Int,
    val email: String,
    val nombre: String,
)

// Favorites
data class ListaFavoritosResponse(val data: List<FavoritoItem>)

data class FavoritoItem(
    val lugarId: Int,
    val lugar: Lugar?,
)

data class AddFavoritoBody(
    @SerializedName("lugarId") val lugarId: Int,
)

data class CreatedFavoritoResponse(val data: CreatedFavoritoData?)
data class CreatedFavoritoData(val usuarioId: Int, val lugarId: Int)

data class CreatedPlaceResponse(val data: Lugar)
data class DeletePlaceResponse(val data: DeletePlaceData)
data class DeletePlaceData(val id: Int)
data class VisitResponse(val data: VisitData)
data class VisitData(val visitas: Int)

data class ErrorResponse(val error: String)

// Comments
data class ListaComentariosResponse(val data: List<Comentario>)

data class Comentario(
    val id: Int,
    val lugarId: Int,
    val usuarioId: Int,
    val usuarioNombre: String,
    val texto: String,
    val calificacion: Int,
    val fecha: String,
)

data class AddComentarioBody(
    val texto: String,
    val calificacion: Int = 5,
)

data class ComentarioCreatedResponse(val data: Comentario)

// Routes
data class RutaResponse(
    val code: String,
    val routes: List<Ruta>?,
)

data class Ruta(
    val geometry: RutaGeometry,
    val distance: Double,
    val duration: Double,
)

data class RutaGeometry(
    val coordinates: List<List<Double>>,
    val type: String,
)
