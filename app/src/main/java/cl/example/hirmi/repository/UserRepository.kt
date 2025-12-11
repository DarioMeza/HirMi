package cl.example.hirmi.repository

import cl.example.hirmi.api.ApiUser
import cl.example.hirmi.api.FollowRequest
import cl.example.hirmi.api.FollowResponse
import cl.example.hirmi.api.RetrofitClient
import cl.example.hirmi.model.User

class UserRepository(private val dao: UserDao) {

    // === LOCAL: Room (registro, login, sesión) ===

    suspend fun addUser(user: User) {
        dao.insert(user)
    }

    suspend fun updateUserLocal(user: User) {
        dao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        dao.delete(user)
    }

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

    // === REMOTO: obtener usuarios desde la API externa (MockAPI) ===
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

    // === REMOTO: FOLLOWS en MockAPI ===

    suspend fun getFollowsForUser(localUserId: String): Result<List<FollowResponse>> {
        return try {
            val follows = RetrofitClient.apiService.getFollows(followerId = localUserId)
            Result.success(follows)
        } catch (e: Exception) {
            println("Error al obtener follows: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun followUser(localUserId: String, remoteUserId: String): Result<FollowResponse> {
        return try {
            val request = FollowRequest(
                followerId = localUserId,
                followedId = remoteUserId
            )
            val response = RetrofitClient.apiService.createFollow(request)
            Result.success(response)
        } catch (e: Exception) {
            println("Error al crear follow: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun unfollow(followId: String): Result<Unit> {
        return try {
            RetrofitClient.apiService.deleteFollow(followId)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error al eliminar follow: ${e.message}")
            Result.failure(e)
        }
    }
}
