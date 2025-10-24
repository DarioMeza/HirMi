package cl.example.hirmi.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cl.example.hirmi.R
import cl.example.hirmi.model.User
import cl.example.hirmi.viewmodel.UserViewModel
import java.util.*

@Composable
fun RegisterScreen(navController: NavController, viewModel: UserViewModel) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }

    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                if (viewModel.register(user)) {
                    navController.navigate("login")
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Registrarse")
        }

        if (!error.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Ya tienes cuenta? ", fontSize = 15.sp)
            Text(
                text = "Inicia sesión aquí",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    viewModel.clearError()
                    navController.navigate("login")
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
