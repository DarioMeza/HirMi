package cl.example.hirmi.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 🚨 IMPORTANTE:
    // Reemplaza este string por la BASE_URL real de tu MockAPI.
    // Debe terminar en "/"
    private const val BASE_URL = "https://TU-MOCKAPI-BASE-URL/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Muestra cuerpo completo de las peticiones/respuestas en Logcat.
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
