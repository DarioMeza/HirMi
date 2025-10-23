package cl.example.hirmi.viewmodel

import androidx.lifecycle.ViewModel
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    private val _users = MutableStateFlow(UserRepository.getUsers())
    val users = _users.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _scanned = MutableStateFlow(false)
    val scanned = _scanned.asStateFlow()

    private val _lastDistance = MutableStateFlow(0)
    val lastDistance = _lastDistance.asStateFlow()

    // === Limpiar error ===
    fun clearError() {
        _error.value = null
    }

    // === Registro de usuario ===
    fun register(user: User): Boolean {

        if (user.firstName.isBlank() || user.lastName.isBlank() ||
            user.username.isBlank() || user.email.isBlank() ||
            user.password.isBlank() || user.birthdate.isBlank()) {
            _error.value = "Todos los campos son obligatorios."
            return false
        }

        if (user.firstName.length < 2 || user.lastName.length < 2) {
            _error.value = "El nombre y apellido deben tener al menos 2 caracteres."
            return false
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        if (!emailRegex.matches(user.email)) {
            _error.value = "El correo electrónico no es válido."
            return false
        }

        if (user.username.length < 4) {
            _error.value = "El nombre de usuario debe tener al menos 4 caracteres."
            return false
        }

        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$")
        if (!passwordRegex.matches(user.password)) {
            _error.value =
                "La contraseña debe tener al menos 6 caracteres, con una mayúscula, una minúscula y un número."
            return false
        }

        val dateRegex = Regex("^(\\d{2}/\\d{2}/\\d{4}|\\d{4}/\\d{2}/\\d{2})$")
        if (!dateRegex.matches(user.birthdate)) {
            _error.value =
                "La fecha de nacimiento debe tener un formato válido (dd/mm/aaaa o aaaa/mm/dd)."
            return false
        }

        val existingUser = UserRepository.getUsers().any {
            it.username.equals(user.username, ignoreCase = true) ||
                    it.email.equals(user.email, ignoreCase = true)
        }
        if (existingUser) {
            _error.value = "El nombre de usuario o correo ya están registrados."
            return false
        }

        UserRepository.addUser(user)
        refreshUsers()
        _error.value = null
        return true
    }

    // === Login ===
    fun login(username: String, password: String): Boolean {

        if (username.isBlank() || password.isBlank()) {
            _error.value = "Debes ingresar tu usuario y contraseña."
            return false
        }

        if (username.length < 4) {
            _error.value = "El nombre de usuario debe tener al menos 4 caracteres."
            return false
        }

        if (password.length < 6) {
            _error.value = "La contraseña debe tener al menos 6 caracteres."
            return false
        }

        val found = UserRepository.getUsers().find {
            it.username.equals(username, ignoreCase = true)
        }

        if (found == null) {
            _error.value = "El usuario no existe o es incorrecto."
            return false
        }

        if (found.password != password) {
            _error.value = "La contraseña es incorrecta."
            return false
        }

        _currentUser.value = found
        _error.value = null
        return true
    }

    // === Filtrado por distancia ===
    fun filterByDistance(maxDistance: Int) {
        _users.value = UserRepository.getUsers().filter { it.distance <= maxDistance }
        _scanned.value = true
        _lastDistance.value = maxDistance
    }

    private fun refreshUsers() {
        _users.value = UserRepository.getUsers()
    }

    fun resetScan() {
        _scanned.value = false
        _lastDistance.value = 0
        refreshUsers()
    }
}
