package cl.example.hirmi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.example.hirmi.repository.UserRepository
import cl.example.hirmi.R as res

@Composable
fun LoginScreen(navController: NavController) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }
    var shouldNavigate by remember { mutableStateOf(false) }

    // Errores individuales
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val users = remember { UserRepository.getUsers() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = res.drawable.icono),
            contentDescription = "Logo de la app",
            modifier = Modifier
                .size(220.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Inicio de sesión",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = null // limpia el error al escribir
            },
            label = { Text("Username") },
            singleLine = true,
            isError = usernameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (usernameError != null) {
            Text(usernameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError != null) {
            Text(passwordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            var valid = true

            // Validaciones
            if (username.isBlank()) {
                usernameError = "El campo no puede estar vacío"
                valid = false
            } else if (!username.matches(Regex("^[a-zA-Z0-9._-]{3,}$"))) {
                usernameError = "Usuario inválido (mínimo 3 caracteres)"
                valid = false
            }

            if (password.isBlank()) {
                passwordError = "El campo no puede estar vacío"
                valid = false
            } else if (password.length < 6) {
                passwordError = "La contraseña debe tener al menos 6 caracteres"
                valid = false
            }

            // Solo si pasa las validaciones continúa
            if (valid) {
                val userFound = users.find { it.username == username && it.password == password }

                if (userFound != null) {
                    dialogText = "Bienvenido ${userFound.firstName} ${userFound.lastName}!"
                    shouldNavigate = true
                } else {
                    dialogText = "Usuario o contraseña incorrectos."
                    shouldNavigate = false
                }

                showDialog = true
            }
        }) {
            Text("Iniciar sesión")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Resultado del Login") },
            text = { Text(dialogText) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (shouldNavigate) {
                        shouldNavigate = false
                        navController.navigate("home")
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}
