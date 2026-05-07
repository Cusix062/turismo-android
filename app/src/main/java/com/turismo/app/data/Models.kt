package com.turismo.app.data

import com.google.gson.annotations.SerializedName

data class ListaLugaresResponse(val data: List<Lugar>)

data class Lugar(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val lat: Double?,
    val lng: Double?,
    val imagen: String? = null,
    val popularidad: Int? = null,
    val fechaCreacion: String? = null,
    val direccion: String? = null,
    val horario: String? = null,
)

data class ListaUsuariosResponse(val data: List<Usuario>)

data class Usuario(
    val id: Int,
    val email: String,
    val nombre: String,
)

data class ListaFavoritosResponse(val data: List<FavoritoItem>)

data class FavoritoItem(
    val lugarId: Int,
    val lugar: Lugar?,
)

data class AddFavoritoBody(
    @SerializedName("lugarId") val lugarId: Int,
)

data class CreatedFavoritoResponse(
    val data: CreatedFavoritoData?,
)

data class CreatedFavoritoData(
    val usuarioId: Int,
    val lugarId: Int,
)

data class SearchSuggestion(
    val id: Int,
    val nombre: String,
    val categoria: String,
)

data class SearchResponse(val data: List<SearchSuggestion>)

data class PlaceDetailResponse(
    val data: Lugar?,
)
