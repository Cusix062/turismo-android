package com.turismo.app.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TurismoApi {

    @GET("api/lugares")
    suspend fun getLugares(): ListaLugaresResponse

    @GET("api/lugares/{id}")
    suspend fun getLugarById(@Path("id") id: Int): PlaceDetailResponse

    @GET("api/lugares/search")
    suspend fun searchLugares(@Query("q") query: String): SearchResponse

    @GET("api/lugares/populares")
    suspend fun getLugaresPopulares(@Query("lat") lat: Double? = null, @Query("lng") lng: Double? = null): ListaLugaresResponse

    @GET("api/lugares/nuevos")
    suspend fun getLugaresNuevos(): ListaLugaresResponse

    @GET("api/lugares/categoria/{categoria}")
    suspend fun getLugaresPorCategoria(@Path("categoria") categoria: String): ListaLugaresResponse

    @GET("api/usuarios/{id}/favoritos")
    suspend fun getFavoritos(@Path("id") usuarioId: Int): ListaFavoritosResponse

    @POST("api/usuarios/{usuarioId}/favoritos")
    suspend fun addFavorito(
        @Path("usuarioId") usuarioId: Int,
        @Body body: AddFavoritoBody,
    ): CreatedFavoritoResponse

    @DELETE("api/usuarios/{usuarioId}/favoritos/{lugarId}")
    suspend fun removeFavorito(
        @Path("usuarioId") usuarioId: Int,
        @Path("lugarId") lugarId: Int,
    ): CreatedFavoritoResponse
}
