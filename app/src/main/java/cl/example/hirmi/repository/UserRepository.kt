package cl.example.hirmi.repository

import cl.example.hirmi.model.User
import cl.example.hirmi.model.Song

object UserRepository {
    private val users = mutableListOf(
        //Usuarios de prueba
        User(
            id = "1",
            firstName = "Dario",
            lastName = "Meza",
            username = "dario",
            email = "dario.meza.ariel@gmial.com",
            password = "123456",
            birthdate = "2005/09/10",
            song = Song("Blinding lights", "The Weekend", "After Hours", "Pop", 213),
            distance = 10
        ),

        // Otro usuario de prueba
        User(
            id = "2",
            firstName = "Benjamin",
            lastName = "Vidal",
            username = "bxnjxtv",
            email = "ben.vidals@duocuc.cl",
            password = "123456",
            birthdate = "2005/09/10",
            song = Song("Como te va", "Cris mj", "Como te va", "Reggeaton", 105),
            distance = 15
        )
    )

    fun getUsers(maxDistance: Int): List<User> {
        // Filtra los usuarios según la distancia máxima proporcionada
        return users.filter { it.distance <= maxDistance }
    }
    fun getUsers(): List<User> {
        // Ahora devuelve la lista mutable
        return users
    }

    // 2. Agrega esta nueva función para añadir usuarios
    fun addUser(user: User) {
        users.add(user)
    }
}
