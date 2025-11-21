package cl.example.hirmi.repository

import cl.example.hirmi.api.ApiUser
import cl.example.hirmi.api.RetrofitClient
import cl.example.hirmi.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {

    fun getUsersStream(): Flow<List<User>> = dao.getAll()

    fun getUsersByDistanceStream(maxDistance: Int): Flow<List<User>> =
        dao.getByDistance(maxDistance)

    suspend fun addUser(user: User) {
        dao.insert(user)
    }

    // === Eliminar un usuario específico ===
    suspend fun deleteUser(user: User) {
        dao.delete(user)
    }

    // === Eliminar todos los usuarios ===
    suspend fun deleteAllUsers() {
        dao.deleteAll()
    }

    suspend fun existsUsername(username: String): Boolean =
        dao.findByUsername(username) != null

    suspend fun existsEmail(email: String): Boolean =
        dao.findByEmail(email) != null

    suspend fun findByCredentials(username: String, password: String): User? =
        dao.login(username, password)

    suspend fun getUserById(id: String): User? =
        dao.findById(id)

    // === REMOTO: obtener usuarios desde la API externa ===
    suspend fun scanRemoteUsers(maxDistance: Int): Result<List<ApiUser>> {
        return try {
            val allUsers = RetrofitClient.apiService.getUsers()
            val filtered = allUsers.filter { it.distance <= maxDistance }
            Result.success(filtered)
        } catch (e: Exception) {
            println("Error al obtener usuarios remotos: ${e.message}")
            Result.failure(e)
        }
    }
}
