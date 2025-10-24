package cl.example.hirmi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository
import kotlinx.coroutines.flow.*
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

    init {
        // Suscribirse a todos los usuarios (se actualiza en tiempo real)
        viewModelScope.launch {
            repo.getUsersStream().collect { _users.value = it }
        }
    }

    fun clearError() {
        _error.value = null
    }

    // === Registro de usuario (usa suspend) ===
    suspend fun register(user: User): Boolean {
        if (user.firstName.isBlank() || user.lastName.isBlank() ||
            user.username.isBlank() || user.email.isBlank() ||
            user.password.isBlank() || user.birthdate.isBlank()) {
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

    // === Login (usa suspend) ===
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

    // === Filtrado por distancia (con corutina) ===
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

    fun resetScan() {
        _scanned.value = false
        _lastDistance.value = 0
        viewModelScope.launch {
            repo.getUsersStream().collect { _users.value = it }
        }
    }
}
