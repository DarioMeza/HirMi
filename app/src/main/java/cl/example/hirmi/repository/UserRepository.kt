package cl.example.hirmi.repository

import cl.example.hirmi.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {

    fun getUsersStream(): Flow<List<User>> = dao.getAll()

    fun getUsersByDistanceStream(maxDistance: Int): Flow<List<User>> =
        dao.getByDistance(maxDistance)

    suspend fun addUser(user: User) {
        dao.insert(user)
    }

    suspend fun existsUsername(username: String): Boolean =
        dao.findByUsername(username) != null

    suspend fun existsEmail(email: String): Boolean =
        dao.findByEmail(email) != null

    suspend fun findByCredentials(username: String, password: String): User? =
        dao.login(username, password)
}
