package cl.example.hirmi.repository

import cl.example.hirmi.model.User
import cl.example.hirmi.model.Song
object UserRepository {
    fun getUsers(): List<User> {
        return listOf(
            User(
                id = "1",
                firstName = "Dario",
                lastName = "Meza",
                username = "malditomotherfucker",
                email = "dario.meza.ariel@gmial.com",
                password = "123456",
                birthdate = "2005/09/10",
                song = Song("Blinding lights", "The Weekend", "After Hours", "Pop", 213),
                distance = 100
            )
        )
    }
}


