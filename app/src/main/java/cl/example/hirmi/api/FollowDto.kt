package cl.example.hirmi.api

import com.squareup.moshi.Json

// Body que ENVIAMOS al hacer follow
data class FollowRequest(
    @Json(name = "followerId") val followerId: String,   // ID del usuario local (Room) como String
    @Json(name = "followedId") val followedId: String    // ID del usuario remoto (ApiUser.id)
)

// Respuesta que RECIBIMOS desde MockAPI
data class FollowResponse(
    @Json(name = "id") val id: String,                   // ID del follow en MockAPI
    @Json(name = "followerId") val followerId: String,
    @Json(name = "followedId") val followedId: String,
    @Json(name = "createdAt") val createdAt: String? = null
)
