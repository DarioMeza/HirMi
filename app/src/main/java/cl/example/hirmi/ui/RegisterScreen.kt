package cl.example.hirmi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.example.hirmi.R
import cl.example.hirmi.model.User
import cl.example.hirmi.repository.UserRepository
import java.util.*
import java.util.regex.Pattern

@Composable
fun RegisterScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    fun validarCampos(): Boolean {
        if (firstName.isBlank() || lastName.isBlank() || username.isBlank() ||
            email.isBlank() || password.isBlank() || birthdate.isBlank()) {
            errorMessage = "Por favor, completa todos los campos."
            return false
        }

        // Validar email con expresión regular
        val emailRegex = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        if (!emailRegex.matcher(email).matches()) {
            errorMessage = "Correo electrónico inválido."
            return false
        }

        // Validar contraseña (mínimo 6 caracteres)
        if (password.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres."
            return false
        }

        // Validar formato de fecha (dd/mm/aaaa)
        val dateRegex = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$")
        if (!dateRegex.matcher(birthdate).matches()) {
            errorMessage = "Formato de fecha inválido. Usa dd/mm/aaaa."
            return false
        }

        errorMessage = ""
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícono ---------------------------------------------------
            Image(
                painter = painterResource(id = R.drawable.icono),
                contentDescription = "Icono de bienvenida",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registro de Usuario",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campos -------------------------------------------------
            TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(0.9f))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellido") }, singleLine = true, modifier = Modifier.fillMaxWidth(0.9f))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = username, onValueChange = { username = it }, label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth(0.9f))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(0.9f))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(0.9f))
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = birthdate, onValueChange = { birthdate = it }, label = { Text("Fecha de nacimiento (dd/mm/aaaa)") }, singleLine = true, modifier = Modifier.fillMaxWidth(0.9f))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validarCampos()) {
                        val user = User(
                            id = UUID.randomUUID().toString(),
                            firstName = firstName,
                            lastName = lastName,
                            username = username,
                            email = email,
                            password = password,
                            birthdate = birthdate,
                            song = null,
                            distance = 0
                        )
                        UserRepository.addUser(user)
                        navController.navigate("home")
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Registrarse")
            }

            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
