package cl.example.hirmi.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ===================== USERS (ya existente) =====================

    // Obtiene TODOS los usuarios de la API.
    // El filtrado se hace en el repositorio.
    @GET("users")
    suspend fun getUsers(): List<ApiUser>

    // ===================== FOLLOWS (nuevo) =====================

    // Obtener TODOS los follows de un usuario local:
    // GET /follows?followerId=123
    @GET("follows")
    suspend fun getFollows(
        @Query("followerId") followerId: String
    ): List<FollowResponse>

    // Crear un follow (seguir a un usuario remoto)
    // POST /follows
    @POST("follows")
    suspend fun createFollow(
        @Body request: FollowRequest
    ): FollowResponse

    // Eliminar un follow (dejar de seguir)
    // DELETE /follows/{id}
    @DELETE("follows/{id}")
    suspend fun deleteFollow(
        @Path("id") followId: String
    )
}
