package cl.example.hirmi.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT

interface ApiService {

    @GET("users")
    suspend fun getUsers(): List<ApiUser>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: ApiUser
    ): ApiUser

    @GET("follows")
    suspend fun getFollows(
        @Query("followerId") followerId: String
    ): List<FollowResponse>

    @POST("follows")
    suspend fun createFollow(
        @Body request: FollowRequest
    ): FollowResponse

    @DELETE("follows/{id}")
    suspend fun deleteFollow(
        @Path("id") followId: String
    )
}
