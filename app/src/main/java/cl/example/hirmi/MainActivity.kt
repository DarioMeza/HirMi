package cl.example.hirmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import cl.example.hirmi.repository.AppDatabase
import cl.example.hirmi.repository.UserRepository
import cl.example.hirmi.ui.HomeScreen
import cl.example.hirmi.ui.LoginScreen
import cl.example.hirmi.ui.RegisterScreen
import cl.example.hirmi.ui.theme.HirMiTheme
import cl.example.hirmi.viewmodel.UserViewModel
import cl.example.hirmi.viewmodel.UserViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === 1️⃣ Crear la base de datos ===
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "hirmi_db"
        ).build()

        // === 2️⃣ Crear el repositorio con el DAO ===
        val repo = UserRepository(db.userDao())

        setContent {
            HirMiTheme {
                val navController = rememberNavController()

                // === 3️⃣ Instanciar el ViewModel con Factory ===
                val viewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(repo)
                )

                // === 4️⃣ Navegación ===
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController, viewModel) }
                    composable("register") { RegisterScreen(navController, viewModel) }
                    composable("home") { HomeScreen(navController, viewModel) }
                }
            }
        }
    }
}
