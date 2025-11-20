package cl.example.hirmi.api

import retrofit2.http.GET

interface ApiService {

    // Obtiene TODOS los usuarios de la API.
    // El filtrado por distancia lo haremos en el repositorio.
    @GET("users")
    suspend fun getUsers(): List<ApiUser>
}
