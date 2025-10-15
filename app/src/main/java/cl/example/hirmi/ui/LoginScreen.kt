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

    val users = remember { UserRepository.getUsers() } // lista de usuarios del repo

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

        Text(text = "Login", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val userFound = users.find { it.username == username && it.password == password }

            if (userFound != null) {
                dialogText = "Bienvenido ${userFound.firstName} ${userFound.lastName}!"
            } else {
                dialogText = "Usuario o contraseña incorrectos."
            }

            showDialog = true
        }) {
            Text("Iniciar sesión")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Resultado del Login") },
            text = { Text(dialogText) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}



