package com.turismo.app.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface TurismoApi {
    // Lugares
    @GET("api/lugares")
    suspend fun getLugares(): ListaLugaresResponse

    @GET("api/lugares/{id}")
    suspend fun getLugarById(@Path("id") id: Int): PlaceDetailResponse

    @GET("api/lugares/search")
    suspend fun searchLugares(@Query("q") query: String): SearchResponse

    @GET("api/lugares/populares")
    suspend fun getLugaresPopulares(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
    ): ListaLugaresResponse

    @GET("api/lugares/nuevos")
    suspend fun getLugaresNuevos(): ListaLugaresResponse

    @GET("api/lugares/categoria/{categoria}")
    suspend fun getLugaresPorCategoria(@Path("categoria") categoria: String): ListaLugaresResponse

    @GET("api/lugares/cercanos")
    suspend fun getLugaresCercanos(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radio") radio: Double = 10.0,
    ): ListaLugaresResponse

    @GET("api/lugares/recomendados")
    suspend fun getLugaresRecomendados(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limite") limite: Int = 10,
    ): ListaLugaresResponse

    // Lugares CRUD (auth required)
    @Multipart
    @POST("api/lugares")
    suspend fun createLugar(
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody,
        @Part("direccion") direccion: RequestBody? = null,
        @Part("horario") horario: RequestBody? = null,
        @Part imagen: MultipartBody.Part? = null,
    ): CreatedPlaceResponse

    @Multipart
    @PUT("api/lugares/{id}")
    suspend fun updateLugar(
        @Path("id") id: Int,
        @Part("nombre") nombre: RequestBody? = null,
        @Part("descripcion") descripcion: RequestBody? = null,
        @Part("categoria") categoria: RequestBody? = null,
        @Part("lat") lat: RequestBody? = null,
        @Part("lng") lng: RequestBody? = null,
        @Part("direccion") direccion: RequestBody? = null,
        @Part("horario") horario: RequestBody? = null,
        @Part imagen: MultipartBody.Part? = null,
    ): CreatedPlaceResponse

    @DELETE("api/lugares/{id}")
    suspend fun deleteLugar(@Path("id") id: Int): DeletePlaceResponse

    // Visitas
    @POST("api/lugares/{id}/visitar")
    suspend fun visitarLugar(@Path("id") lugarId: Int): VisitResponse

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body body: AuthRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: AuthRequest): AuthResponse

    // Favoritos (autenticados)
    @GET("api/favoritos")
    suspend fun getFavoritos(): ListaFavoritosResponse

    @POST("api/favoritos")
    suspend fun addFavorito(@Body body: AddFavoritoBody): CreatedFavoritoResponse

    @DELETE("api/favoritos/{lugarId}")
    suspend fun removeFavorito(@Path("lugarId") lugarId: Int): CreatedFavoritoResponse

    // Comentarios
    @GET("api/lugares/{id}/comentarios")
    suspend fun getComentarios(@Path("id") lugarId: Int): ListaComentariosResponse

    @POST("api/lugares/{id}/comentarios")
    suspend fun addComentario(
        @Path("id") lugarId: Int,
        @Body body: AddComentarioBody,
    ): ComentarioCreatedResponse

    // Rutas
    @GET("api/rutas")
    suspend fun getRuta(
        @Query("origen") origen: String,
        @Query("destino") destino: String,
    ): RutaResponse
}
