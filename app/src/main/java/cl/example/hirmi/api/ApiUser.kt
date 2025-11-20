package cl.example.hirmi.api

import cl.example.hirmi.model.Song

data class ApiUser(
    val id: String,
    val firstName: String,
    val lastName: String,
    val bio: String?,
    val distance: Int,
    val song: Song?
)
