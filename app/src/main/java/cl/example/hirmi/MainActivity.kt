package cl.example.hirmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.example.hirmi.ui.*
import cl.example.hirmi.viewmodel.UserViewModel
import cl.example.hirmi.ui.theme.HirMiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HirMiTheme {
                val nav = rememberNavController()
                val userViewModel: UserViewModel = viewModel()

                NavHost(navController = nav, startDestination = "welcome") {
                    composable("welcome") { WelcomeScreen(nav) }
                    composable("login") { LoginScreen(nav, userViewModel) }
                    composable("register") { RegisterScreen(nav, userViewModel) }
                    composable("home") { HomeScreen(nav, userViewModel) }
                }
            }
        }
    }
}
