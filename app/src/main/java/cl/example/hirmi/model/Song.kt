package cl.example.hirmi.model

data class Song(
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val duration: Int,
    val coverURL: String? = null
)
