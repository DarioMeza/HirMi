package cl.example.hirmi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.example.hirmi.api.ApiUser
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repo: UserRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _scanned = MutableStateFlow(false)
    val scanned = _scanned.asStateFlow()

    private val _lastDistance = MutableStateFlow(0)
    val lastDistance = _lastDistance.asStateFlow()

    // === Estado de usuarios remotos (API externa) ===
    private val _remoteUsers = MutableStateFlow<List<ApiUser>>(emptyList())
    val remoteUsers = _remoteUsers.asStateFlow()

    private val _remoteLoading = MutableStateFlow(false)
    val remoteLoading = _remoteLoading.asStateFlow()

    private val _remoteError = MutableStateFlow<String?>(null)
    val remoteError = _remoteError.asStateFlow()

    init {
        // Carga inicial de usuarios en tiempo real
        viewModelScope.launch {
            repo.getUsersStream().collect { _users.value = it }
        }
    }

    // === Limpiar error local ===
    fun clearError() {
        _error.value = null
    }

    // === Limpiar error remoto ===
    fun clearRemoteError() {
        _remoteError.value = null
    }

    // === Registro de usuario ===
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
        refreshUsers()
        return true
    }

    // === Login ===
    suspend fun login(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            _error.value = "Debes ingresar tu usuario y contraseña."
            return false
        }

        val found = repo.findByCredentials(username, password)
        return if (found != null) {
            _currentUser.value = found
            _error.value = null
            true
        } else {
            _error.value = "Usuario o contraseña incorrectos."
            false
        }
    }

    // === Cerrar sesión ===
    fun logout() {
        _currentUser.value = null
        _error.value = null
        _scanned.value = false
        _lastDistance.value = 0
    }

    // === Filtrado por distancia (local, Room) ===
    fun filterByDistance(maxDistance: Int) {
        viewModelScope.launch {
            repo.getUsersByDistanceStream(maxDistance).collect { list ->
                val me = _currentUser.value
                _users.value = if (me == null) list else list.filter { it.id != me.id }
            }
        }
        _scanned.value = true
        _lastDistance.value = maxDistance
    }

    // === Eliminar usuario actual ===
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repo.deleteUser(user)
            if (_currentUser.value?.id == user.id) {
                logout()
            }
            refreshUsers()
        }
    }

    // === Eliminar todos los usuarios ===
    fun deleteAllUsers() {
        viewModelScope.launch {
            repo.deleteAllUsers()
            logout()
            refreshUsers()
        }
    }

    // === Refrescar lista de usuarios ===
    private fun refreshUsers() {
        viewModelScope.launch {
            repo.getUsersStream().collect { _users.value = it }
        }
    }

    // === Reiniciar escaneo local ===
    fun resetScan() {
        _scanned.value = false
        _lastDistance.value = 0
        refreshUsers()
    }

    // === Generar usuarios iniciales simulados ===
    fun generateInitialUsersIfEmpty() {
        viewModelScope.launch {
            val currentUsers = _users.value
            if (currentUsers.isEmpty()) {
                val nombres = listOf(
                    "Dario", "Camila", "Mateo", "Sofia", "Benjamin",
                    "Lucas", "Valentina", "Nicolas", "Isabella", "Sebastian"
                )
                val apellidos = listOf(
                    "Meza", "Rojas", "Vidal", "Gonzalez", "Torres",
                    "Morales", "Perez", "Romero", "Fernandez", "Sanchez"
                )
                val canciones = listOf(
                    Triple("Blinding Lights", "The Weeknd", "After Hours"),
                    Triple("Shape of You", "Ed Sheeran", "Divide"),
                    Triple("Como Te Va", "Cris MJ", "Single"),
                    Triple("Levitating", "Dua Lipa", "Future Nostalgia"),
                    Triple("Dance Monkey", "Tones and I", "The Kids Are Coming")
                )
                val generos = listOf("Pop", "Reggaeton", "Rock", "Indie", "Electrónica")

                repeat(15) {
                    val nombre = nombres.random()
                    val apellido = apellidos.random()
                    val cancion = canciones.random()

                    val user = User(
                        id = java.util.UUID.randomUUID().toString(),
                        firstName = nombre,
                        lastName = apellido,
                        username = "${nombre.lowercase()}${apellido.lowercase().first()}${(100..999).random()}",
                        email = "${nombre.lowercase()}.${apellido.lowercase()}@example.com",
                        password = "Test123A",
                        birthdate = "1990/01/01",
                        song = cl.example.hirmi.model.Song(
                            title = cancion.first,
                            artist = cancion.second,
                            album = cancion.third,
                            genre = generos.random(),
                            duration = (120..280).random()
                        ),
                        distance = (1..100).random()
                    )
                    repo.addUser(user)
                }
            }
        }
    }

    // === Escaneo remoto: API externa (MockAPI) ===
    fun scanRemoteUsers(maxDistance: Int) {
        viewModelScope.launch {
            _remoteLoading.value = true
            _remoteError.value = null

            val result = repo.scanRemoteUsers(maxDistance)

            result
                .onSuccess { users ->
                    _remoteUsers.value = users
                }
                .onFailure { e ->
                    _remoteError.value = "Error al obtener usuarios remotos: ${e.message}"
                }

            _remoteLoading.value = false
        }
    }
}
