package cl.example.hirmi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.example.hirmi.api.ApiUser
import cl.example.hirmi.api.FollowResponse
import cl.example.hirmi.datastore.SessionDataStore
import cl.example.hirmi.model.Song
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserViewModel(
    private val repo: UserRepository,
    private val session: SessionDataStore
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _scanned = MutableStateFlow(false)
    val scanned = _scanned.asStateFlow()

    private val _lastDistance = MutableStateFlow(0)
    val lastDistance = _lastDistance.asStateFlow()

    private val _remoteUsers = MutableStateFlow<List<ApiUser>>(emptyList())
    val remoteUsers = _remoteUsers.asStateFlow()

    private val _remoteLoading = MutableStateFlow(false)
    val remoteLoading = _remoteLoading.asStateFlow()

    private val _remoteError = MutableStateFlow<String?>(null)
    val remoteError = _remoteError.asStateFlow()

    private val _isSessionChecked = MutableStateFlow(false)
    val isSessionChecked = _isSessionChecked.asStateFlow()

    private val _remoteFollows = MutableStateFlow<Map<String, FollowResponse>>(emptyMap())
    val remoteFollows = _remoteFollows.asStateFlow()

    private val _globalFollows = MutableStateFlow<List<FollowResponse>>(emptyList())
    val globalFollows = _globalFollows.asStateFlow()

    private val _allRemoteUsers = MutableStateFlow<List<ApiUser>>(emptyList())
    private val allRemoteById: Map<String, ApiUser>
        get() = _allRemoteUsers.value.associateBy { it.id }

    init {
        viewModelScope.launch {
            val savedUserId = session.loggedUserId.first()
            if (savedUserId != null) {
                val user = repo.getUserById(savedUserId)
                _currentUser.value = user

                user?.let {
                    loadRemoteFollowsForUser(it.id)
                    preloadRemoteUsersForFriends()
                }
            }
            // preload all follows to compute counters
            preloadAllFollows()
            // NUEVO: precargar todos los usuarios remotos para resolución por ID
            preloadAllRemoteUsers()
            _isSessionChecked.value = true
        }
    }

    private fun preloadAllFollows() {
        viewModelScope.launch {
            val result = repo.getAllFollows()
            result.onSuccess { _globalFollows.value = it }
                .onFailure { _remoteError.value = "Error al cargar seguidores: ${it.message}" }
        }
    }

    private fun preloadAllRemoteUsers() {
        viewModelScope.launch {
            val result = repo.getAllRemoteUsers()
            result
                .onSuccess { _allRemoteUsers.value = it }
                .onFailure { _remoteError.value = "Error al cargar usuarios globales: ${it.message}" }
        }
    }

    fun getCountersFor(userId: String): Pair<Int, Int> {
        // returns followersCount to this user, and followingCount by this user
        val followers = _globalFollows.value.count { it.followedId == userId }
        val following = _globalFollows.value.count { it.followerId == userId }
        return followers to following
    }

    // NUEVO: listas reales de seguidores y seguidos para cualquier usuario
    fun getFollowersFor(userId: String): List<ApiUser> {
        val followerIds = _globalFollows.value
            .filter { it.followedId == userId }
            .map { it.followerId }
            .toSet()
        // usar el mapa global primero; si no, caer al listado visible
        val byId = allRemoteById.ifEmpty { _remoteUsers.value.associateBy { it.id } }
        return followerIds.mapNotNull { byId[it] }
    }

    fun getFollowingFor(userId: String): List<ApiUser> {
        val followingIds = _globalFollows.value
            .filter { it.followerId == userId }
            .map { it.followedId }
            .toSet()
        val byId = allRemoteById.ifEmpty { _remoteUsers.value.associateBy { it.id } }
        return followingIds.mapNotNull { byId[it] }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearRemoteError() {
        _remoteError.value = null
    }

    suspend fun register(user: User): Boolean {
        if (user.firstName.isBlank() || user.lastName.isBlank() ||
            user.username.isBlank() || user.email.isBlank() ||
            user.password.isBlank() || user.birthdate.isBlank()
        ) {
            _error.value = "Todos los campos son obligatorios."
            return false
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        if (!emailRegex.matches(user.email)) {
            _error.value = "El correo electrónico no es válido."
            return false
        }

        if (repo.existsUsername(user.username)) {
            _error.value = "El nombre de usuario ya está registrado."
            return false
        }

        if (repo.existsEmail(user.email)) {
            _error.value = "El correo ya está registrado."
            return false
        }

        repo.addUser(user)
        _error.value = null
        return true
    }

    suspend fun login(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            _error.value = "Debes ingresar tu usuario y contraseña."
            return false
        }

        val found = repo.findByCredentials(username, password)
        return if (found != null) {
            _currentUser.value = found
            _error.value = null

            session.saveUserSession(found.id)

            loadRemoteFollowsForUser(found.id)
            preloadRemoteUsersForFriends()

            true
        } else {
            _error.value = "Usuario o contraseña incorrectos."
            false
        }
    }

    fun logout() {
        _currentUser.value = null
        _error.value = null
        _scanned.value = false
        _lastDistance.value = 0
        _remoteUsers.value = emptyList()
        _remoteError.value = null
        _remoteFollows.value = emptyMap()

        viewModelScope.launch {
            session.clearSession()
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repo.deleteUser(user)
            if (_currentUser.value?.id == user.id) {
                logout()
            }
        }
    }

    fun deleteAllUsers() {
        viewModelScope.launch {
            repo.deleteAllUsers()
            logout()
        }
    }

    fun scanRemoteUsers(maxDistance: Int) {
        viewModelScope.launch {
            _remoteLoading.value = true
            _remoteError.value = null

            _scanned.value = true
            _lastDistance.value = maxDistance

            val result = repo.scanRemoteUsers(maxDistance)

            result
                .onSuccess { users ->
                    _remoteUsers.value = users
                    val validIds = users.map { it.id }.toSet()
                    _remoteFollows.value = _remoteFollows.value.filterKeys { it in validIds }
                }
                .onFailure { e ->
                    _remoteError.value = "Error al obtener usuarios remotos: ${e.message}"
                }

            _remoteLoading.value = false
        }
    }

    private fun preloadRemoteUsersForFriends() {
        viewModelScope.launch {
            val result = repo.scanRemoteUsers(100)
            result
                .onSuccess { users ->
                    _remoteUsers.value = users
                }
                .onFailure { e ->
                    _remoteError.value = "Error al precargar usuarios remotos: ${e.message}"
                }
        }
    }

    private fun loadRemoteFollowsForUser(localUserId: String) {
        viewModelScope.launch {
            val result = repo.getFollowsForUser(localUserId)
            result
                .onSuccess { follows ->
                    _remoteFollows.value = follows.associateBy { it.followedId }
                }
                .onFailure { e ->
                    _remoteError.value = "Error al cargar follows: ${e.message}"
                }
        }
    }

    // ✅ Opción A: SOLO ROOM
    fun updateNowPlaying(song: Song) {
        val local = _currentUser.value ?: run {
            _error.value = "Debes iniciar sesión."
            return
        }

        viewModelScope.launch {
            try {
                val updatedLocal = local.copy(song = song)
                repo.updateUserLocal(updatedLocal)
                _currentUser.value = updatedLocal
            } catch (e: Exception) {
                _error.value = "No se pudo actualizar la canción: ${e.message}"
            }
        }
    }

    fun toggleFollowFor(remoteUser: ApiUser) {
        val localUserId = _currentUser.value?.id ?: run {
            _remoteError.value = "Debes iniciar sesión para seguir usuarios."
            return
        }

        val currentMap = _remoteFollows.value
        val existingFollow = currentMap[remoteUser.id]

        if (existingFollow != null) {
            viewModelScope.launch {
                val result = repo.unfollow(existingFollow.id)
                result
                    .onSuccess {
                        _remoteFollows.value = _remoteFollows.value - remoteUser.id
                        _globalFollows.value = _globalFollows.value.filterNot { it.id == existingFollow.id }
                        // refrescar globales
                        preloadAllFollows()
                    }
                    .onFailure { e ->
                        _remoteError.value = "Error al dejar de seguir: ${e.message}"
                    }
            }
        } else {
            viewModelScope.launch {
                val result = repo.followUser(localUserId, remoteUser.id)
                result
                    .onSuccess { follow ->
                        _remoteFollows.value = _remoteFollows.value + (remoteUser.id to follow)
                        _globalFollows.value = _globalFollows.value + follow
                        // refrescar globales y usuarios para asegurar que el seguido aparezca en listados
                        preloadAllFollows()
                        preloadAllRemoteUsers()
                    }
                    .onFailure { e ->
                        _remoteError.value = "Error al seguir usuario: ${e.message}"
                    }
            }
        }
    }
}
