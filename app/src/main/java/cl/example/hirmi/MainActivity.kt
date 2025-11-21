package cl.example.hirmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import cl.example.hirmi.datastore.SessionDataStore
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

        // === 3️⃣ Crear DataStore de sesión ===
        val sessionDataStore = SessionDataStore(applicationContext)

        setContent {
            HirMiTheme {
                val navController = rememberNavController()

                // === 4️⃣ Instanciar el ViewModel con Factory (Room + DataStore) ===
                val viewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(repo, sessionDataStore)
                )

                // === 5️⃣ Generar usuarios simulados (solo si la DB está vacía) ===
                viewModel.generateInitialUsersIfEmpty()

                // === 6️⃣ Configurar navegación ===
                NavHost(navController = navController, startDestination = "welcome") {
                    composable("login") { LoginScreen(navController, viewModel) }
                    composable("register") { RegisterScreen(navController, viewModel) }
                    composable("home") { HomeScreen(navController, viewModel) }
                }
            }
        }
    }
}
