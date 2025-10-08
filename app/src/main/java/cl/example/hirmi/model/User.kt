package cl.example.hirmi.model

data class User (
    val id: String,
    val firstName: String,
    val lastName: String,
    var username: String,
    var email: String,
    var password: String,
    val birthdate: String,
    var song: Song?,
    var distance: Int
)