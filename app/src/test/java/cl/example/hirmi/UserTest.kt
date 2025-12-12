package cl.example.hirmi

import cl.example.hirmi.model.User
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTest {

    @Test
    fun username_noDebeEstarVacio() {
        val user = User(
            id = "1",
            firstName = "Pedro",
            lastName = "Pérez",
            username = "pedro",
            email = "pedro@example.com",
            password = "1234",
            birthdate = "2000-01-01",
            song = null,
            distance = 0
        )

        assertTrue(user.username.isNotBlank())
    }
}
