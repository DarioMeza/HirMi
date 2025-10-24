package cl.example.hirmi.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val password: String,
    val birthdate: String,
    @Embedded(prefix = "song_") val song: Song?,
    val distance: Int
)
