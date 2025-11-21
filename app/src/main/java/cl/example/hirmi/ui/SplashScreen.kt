package cl.example.hirmi.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import cl.example.hirmi.viewmodel.UserViewModel

@Composable
fun SplashScreen(navController: NavController, viewModel: UserViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSessionChecked by viewModel.isSessionChecked.collectAsState()

    // Cuando ya se revisó DataStore, decidimos a dónde ir
    LaunchedEffect(isSessionChecked, currentUser) {
        if (isSessionChecked) {
            if (currentUser != null) {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("welcome") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // Pantalla de carga mientras se revisa la sesión
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (!isSessionChecked) {
            CircularProgressIndicator()
        } else {
            Text("Redirigiendo...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
