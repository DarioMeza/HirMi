package cl.example.hirmi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.example.hirmi.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import cl.example.hirmi.R as res

@Composable
fun LoginScreen(navController: NavController, viewModel: UserViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope() // 👈 corrutina para el botón

    // Limpiar error al entrar
    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

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

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch { // ✅ corrutina aquí
                    val success = viewModel.login(username, password)
                    if (success) {
                        navController.navigate("home")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Iniciar sesión")
        }

        if (!error.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                error!!,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Aún no tienes cuenta? ", fontSize = 15.sp)
            Text(
                text = "Regístrate aquí",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    viewModel.clearError()
                    navController.navigate("register")
                }
            )
        }
    }
}
