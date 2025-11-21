package cl.example.hirmi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.example.hirmi.api.ApiUser
import cl.example.hirmi.datastore.SessionDataStore
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

    // === Estado de errores generales (registro/login) ===
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // === Usuario actual (logueado) ===
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // === Estado del escaneo remoto ===
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

    // === Estado de sesión (para saber cuándo ya se revisó DataStore) ===
    private val _isSessionChecked = MutableStateFlow(false)
    val isSessionChecked = _isSessionChecked.asStateFlow()

    init {
        // Revisar sesión guardada en DataStore
        viewModelScope.launch {
            val savedUserId = session.loggedUserId.first()
            if (savedUserId != null) {
                val user = repo.getUserById(savedUserId)
                _currentUser.value = user
            }
            _isSessionChecked.value = true
        }
    }

    // === Limpiar error local (registro / login) ===
    fun clearError() {
        _error.value = null
    }

    // === Limpiar error remoto (API) ===
    fun clearRemoteError() {
        _remoteError.value = null
    }

    // === Registro de usuario (LOCAL: Room) ===
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

    // === Login (LOCAL: Room + DataStore sesión) ===
    suspend fun login(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) {
            _error.value = "Debes ingresar tu usuario y contraseña."
            return false
        }

        val found = repo.findByCredentials(username, password)
        return if (found != null) {
            _currentUser.value = found
            _error.value = null

            // Guardar sesión en DataStore
            session.saveUserSession(found.id)

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
        _remoteUsers.value = emptyList()
        _remoteError.value = null

        viewModelScope.launch {
            session.clearSession()
        }
    }

    // === Eliminar usuario actual ===
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repo.deleteUser(user)
            if (_currentUser.value?.id == user.id) {
                logout()
            }
        }
    }

    // === Eliminar todos los usuarios locales ===
    fun deleteAllUsers() {
        viewModelScope.launch {
            repo.deleteAllUsers()
            logout()
        }
    }

    // === Escaneo remoto: API externa (MockAPI) ===
    fun scanRemoteUsers(maxDistance: Int) {
        viewModelScope.launch {
            _remoteLoading.value = true
            _remoteError.value = null

            // Marcar que se hizo un escaneo y guardar la distancia
            _scanned.value = true
            _lastDistance.value = maxDistance

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
