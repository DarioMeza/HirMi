package cl.example.hirmi.model

object SongCatalog {
    val songs = listOf(
        Song(
            title = "Una Noche en Medellín",
            artist = "Cris MJ",
            album = "Partyson",
            genre = "Urbano",
            duration = 155,
            coverURL = "res:cover_cris_una_noche"
        ),
        Song(
            title = "Gata Only",
            artist = "Cris MJ",
            album = "Single",
            genre = "Urbano",
            duration = 142,
            coverURL = "res:cover_cris_gata_only"
        ),
        Song(
            title = "Marisola",
            artist = "Cris MJ",
            album = "Partyson",
            genre = "Urbano",
            duration = 170,
            coverURL = "res:cover_cris_marisola"
        ),
        Song(
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            genre = "Pop",
            duration = 200,
            coverURL = "res:cover_weeknd_blinding_lights"
        ),
        Song(
            title = "bad guy",
            artist = "Billie Eilish",
            album = "WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?",
            genre = "Pop",
            duration = 194,
            coverURL = "res:cover_bad_guy"
        )
    )
}
