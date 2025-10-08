package cl.example.hirmi.repository

import cl.example.hirmi.model.User
import cl.example.hirmi.model.Song

object UserRepository {
    fun getUsers(): List<User> {
        return listOf(
            //Usuarios de prueba
            User(
                id = "1",
                firstName = "Dario",
                lastName = "Meza",
                username = "malditomotherfucker",
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
    }
}


