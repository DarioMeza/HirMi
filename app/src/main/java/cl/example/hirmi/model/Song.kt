package cl.example.hirmi.model

import java.net.URL

data class Song(
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val duration: Int,
    val coverURL: String? = null
)
