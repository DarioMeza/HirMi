package cl.example.hirmi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.example.hirmi.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: UserViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showConfirmDelete by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("welcome") {
                popUpTo("home") { inclusive = true }
            }
        }
        return
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(currentUser!!)
                    showConfirmDelete = false
                    navController.navigate("welcome") {
                        popUpTo("home") { inclusive = true }
                    }
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${currentUser!!.firstName} ${currentUser!!.lastName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "@${currentUser!!.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Divider()

            Text("Correo: ${currentUser!!.email}")
            Text("Nacimiento: ${currentUser!!.birthdate}")

            currentUser!!.song?.let {
                Divider()
                Text("🎵 Canción")
                Text("${it.title} — ${it.artist}")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("Cerrar sesión")
            }

            OutlinedButton(
                onClick = { showConfirmDelete = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar cuenta", color = Color.Red)
            }
        }
    }
}
